"use client";

import { useEffect, useMemo } from "react";
import { z } from "zod";
import { format, parseISO } from "date-fns";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import {
    getUserDetailsQueryKey,
    type UpdateAccountMutationRequest,
    type UserSummary,
    updateUserRequestGenderEnum,
    useGetUserDetails,
    useUpdateAccount,
} from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import {
    Form,
    FormControl,
    FormDescription,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { Textarea } from "@/components/ui/textarea";
import { DatePicker } from "@/components/date-picker";
import { SelectDropdown } from "@/components/select-dropdown";

function createAccountFormSchema(t: ReturnType<typeof useTranslations>) {
    return z.object({
        firstName: z
            .string()
            .trim()
            .min(1, t("firstNameRequired"))
            .max(100, t("firstNameMax")),
        lastName: z
            .string()
            .trim()
            .min(1, t("lastNameRequired"))
            .max(100, t("lastNameMax")),
        email: z.email(t("emailInvalid")),
        phoneNumber: z.string().max(50, t("phoneNumberMax")).optional(),
        birthDate: z.date(t("birthDateRequired")),
        gender: z.enum(["MALE", "FEMALE"], t("genderRequired")),
        address: z.string().max(500, t("addressMax")).optional(),
        languageKey: z.string().max(20, t("languageMax")).optional(),
        imageUrl: z
            .union([z.literal(""), z.url(t("imageUrlInvalid"))])
            .optional(),
    });
}

type AccountFormValues = z.infer<ReturnType<typeof createAccountFormSchema>>;

const defaultValues: AccountFormValues = {
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    birthDate: new Date("1990-01-01"),
    gender: "MALE",
    address: "",
    languageKey: "",
    imageUrl: "",
};

function normalizeOptional(value?: string) {
    const trimmed = value?.trim();
    return trimmed ? trimmed : undefined;
}

function mapUserToFormValues(user: UserSummary): AccountFormValues {
    return {
        firstName: user.firstName ?? "",
        lastName: user.lastName ?? "",
        email: user.email ?? "",
        phoneNumber: user.phoneNumber ?? "",
        birthDate: parseISO(user.birthDate),
        gender: user.gender,
        address: user.address ?? "",
        languageKey: user.languageKey ?? "",
        imageUrl: user.imageUrl ?? "",
    };
}

function toUpdatePayload(
    values: AccountFormValues
): UpdateAccountMutationRequest {
    return {
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        phoneNumber: normalizeOptional(values.phoneNumber),
        birthDate: format(values.birthDate, "yyyy-MM-dd"),
        gender: values.gender,
        address: normalizeOptional(values.address),
        languageKey: normalizeOptional(values.languageKey),
        imageUrl: normalizeOptional(values.imageUrl),
    };
}

function AccountFormSkeleton() {
    return (
        <div className="space-y-4">
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-24 w-full" />
            <Skeleton className="h-24 w-full" />
        </div>
    );
}

export function AccountForm() {
    const t = useTranslations("SettingsAccount.form");
    const tValidation = useTranslations("SettingsAccount.form.validation");
    const queryClient = useQueryClient();
    const { data: user, isLoading, isError } = useGetUserDetails();
    const updateAccount = useUpdateAccount({
        mutation: {
            onSuccess: (updatedUser) => {
                queryClient.setQueryData(getUserDetailsQueryKey(), updatedUser);
                toast.success(t("toastUpdated"));
            },
            onError: handleServerError,
        },
    });

    const languageOptions = [
        { label: t("fields.languageEnglish"), value: "en" },
        { label: t("fields.languageFrench"), value: "fr" },
    ] as const;

    const genderOptions = [
        { label: t("fields.genderMale"), value: updateUserRequestGenderEnum.MALE },
        {
            label: t("fields.genderFemale"),
            value: updateUserRequestGenderEnum.FEMALE,
        },
    ] as const;

    const accountFormSchema = useMemo(
        () => createAccountFormSchema(tValidation),
        [tValidation]
    );

    const form = useForm<AccountFormValues>({
        resolver: zodResolver(accountFormSchema),
        defaultValues,
        mode: "onChange",
    });

    useEffect(() => {
        if (!user) return;
        form.reset(mapUserToFormValues(user));
    }, [form, user]);

    if (isLoading) {
        return <AccountFormSkeleton />;
    }

    if (isError || !user) {
        return (
            <p className="text-sm text-muted-foreground">
                {t("loadError")} {JSON.stringify(isError)}
            </p>
        );
    }

    return (
        <Form {...form}>
            <form
                onSubmit={form.handleSubmit((values) =>
                    updateAccount.mutate({ data: toUpdatePayload(values) })
                )}
                className="space-y-8"
            >
                <div className="grid gap-6 md:grid-cols-2">
                    <FormField
                        control={form.control}
                        name="firstName"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>{t("fields.firstName")}</FormLabel>
                                <FormControl>
                                    <Input
                                        placeholder={t(
                                            "fields.firstNamePlaceholder"
                                        )}
                                        {...field}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                    <FormField
                        control={form.control}
                        name="lastName"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>{t("fields.lastName")}</FormLabel>
                                <FormControl>
                                    <Input
                                        placeholder={t(
                                            "fields.lastNamePlaceholder"
                                        )}
                                        {...field}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                </div>

                <FormField
                    control={form.control}
                    name="email"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("fields.email")}</FormLabel>
                            <FormControl>
                                <Input {...field} disabled />
                            </FormControl>
                            <FormDescription>
                                {t("fields.emailDescription")}
                            </FormDescription>
                            <FormMessage />
                        </FormItem>
                    )}
                />

                <FormField
                    control={form.control}
                    name="phoneNumber"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("fields.phoneNumber")}</FormLabel>
                            <FormControl>
                                <Input
                                    placeholder={t(
                                        "fields.phoneNumberPlaceholder"
                                    )}
                                    {...field}
                                    value={field.value ?? ""}
                                />
                            </FormControl>
                            <FormDescription>
                                {t("fields.phoneNumberDescription")}
                            </FormDescription>
                            <FormMessage />
                        </FormItem>
                    )}
                />

                <div className="grid gap-6 md:grid-cols-2">
                    <FormField
                        control={form.control}
                        name="birthDate"
                        render={({ field }) => (
                            <FormItem className="flex flex-col">
                                <FormLabel>{t("fields.birthDate")}</FormLabel>
                                <FormControl>
                                    <DatePicker
                                        selected={field.value}
                                        onSelect={(date) =>
                                            field.onChange(date ?? field.value)
                                        }
                                        placeholder={t(
                                            "fields.birthDatePlaceholder"
                                        )}
                                    />
                                </FormControl>
                                <FormDescription>
                                    {t("fields.birthDateDescription")}
                                </FormDescription>
                                <FormMessage />
                            </FormItem>
                        )}
                    />

                    <FormField
                        control={form.control}
                        name="gender"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>{t("fields.gender")}</FormLabel>
                                <SelectDropdown
                                    defaultValue={field.value}
                                    onValueChange={field.onChange}
                                    placeholder={t("fields.genderPlaceholder")}
                                    className="w-full"
                                    items={genderOptions.map((option) => ({
                                        label: option.label,
                                        value: option.value,
                                    }))}
                                    isControlled
                                />
                                <FormDescription>
                                    {t("fields.genderDescription")}
                                </FormDescription>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                </div>

                <FormField
                    control={form.control}
                    name="languageKey"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("fields.language")}</FormLabel>
                            <SelectDropdown
                                defaultValue={field.value || undefined}
                                onValueChange={field.onChange}
                                placeholder={t("fields.languagePlaceholder")}
                                className="w-full"
                                items={languageOptions.map((language) => ({
                                    label: language.label,
                                    value: language.value,
                                }))}
                                isControlled
                            />
                            <FormDescription>
                                {t("fields.languageDescription")}
                            </FormDescription>
                            <FormMessage />
                        </FormItem>
                    )}
                />

                <FormField
                    control={form.control}
                    name="imageUrl"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("fields.imageUrl")}</FormLabel>
                            <FormControl>
                                <Input
                                    placeholder={t(
                                        "fields.imageUrlPlaceholder"
                                    )}
                                    {...field}
                                    value={field.value ?? ""}
                                />
                            </FormControl>
                            <FormDescription>
                                {t("fields.imageUrlDescription")}
                            </FormDescription>
                            <FormMessage />
                        </FormItem>
                    )}
                />

                <FormField
                    control={form.control}
                    name="address"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("fields.address")}</FormLabel>
                            <FormControl>
                                <Textarea
                                    placeholder={t(
                                        "fields.addressPlaceholder"
                                    )}
                                    className="resize-none"
                                    {...field}
                                    value={field.value ?? ""}
                                />
                            </FormControl>
                            <FormDescription>
                                {t("fields.addressDescription")}
                            </FormDescription>
                            <FormMessage />
                        </FormItem>
                    )}
                />

                <Button
                    type="submit"
                    disabled={
                        updateAccount.isPending || !form.formState.isDirty
                    }
                >
                    {updateAccount.isPending
                        ? t("submitPending")
                        : t("submitDefault")}
                </Button>
            </form>
        </Form>
    );
}
