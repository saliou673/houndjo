"use client";

import { useMemo } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { AxiosError } from "axios";
import { useChangePassword } from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { PasswordInput } from "@/components/password-input";

// Mirrors the backend's PasswordChangeRequest constraint.
const PASSWORD_PATTERN =
    /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$/;

function createChangePasswordSchema(t: ReturnType<typeof useTranslations>) {
    return z
        .object({
            currentPassword: z.string().min(1, t("currentPasswordRequired")),
            newPassword: z
                .string()
                .min(8, t("newPasswordTooShort"))
                .regex(PASSWORD_PATTERN, t("newPasswordTooWeak")),
            confirmPassword: z.string().min(1, t("confirmPasswordRequired")),
        })
        .refine((data) => data.newPassword === data.confirmPassword, {
            message: t("passwordsDoNotMatch"),
            path: ["confirmPassword"],
        });
}

type ChangePasswordFormValues = z.infer<
    ReturnType<typeof createChangePasswordSchema>
>;

const defaultValues: ChangePasswordFormValues = {
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
};

export function ChangePasswordSection() {
    const t = useTranslations("SettingsAccount.changePassword");
    const tValidation = useTranslations(
        "SettingsAccount.changePassword.validation"
    );

    const changePasswordSchema = useMemo(
        () => createChangePasswordSchema(tValidation),
        [tValidation]
    );

    const form = useForm<ChangePasswordFormValues>({
        resolver: zodResolver(changePasswordSchema),
        defaultValues,
    });

    const changePassword = useChangePassword({
        mutation: {
            onSuccess: () => {
                toast.success(t("toastUpdated"));
                form.reset(defaultValues);
            },
            onError: (error) => {
                const status =
                    error instanceof AxiosError
                        ? error.response?.status
                        : undefined;

                if (status === 403 || status === 409) {
                    form.setError("currentPassword", {
                        message: t("invalidCurrentPassword"),
                    });
                    return;
                }

                handleServerError(error);
            },
        },
    });

    function onSubmit(values: ChangePasswordFormValues) {
        changePassword.mutate({
            data: {
                currentPassword: values.currentPassword,
                newPassword: values.newPassword,
            },
        });
    }

    return (
        <div className="space-y-4">
            <div>
                <h4 className="text-sm font-medium">{t("title")}</h4>
                <p className="mt-1 text-sm text-muted-foreground">
                    {t("description")}
                </p>
            </div>
            <Form {...form}>
                <form
                    onSubmit={form.handleSubmit(onSubmit)}
                    className="space-y-4"
                >
                    <FormField
                        control={form.control}
                        name="currentPassword"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>
                                    {t("currentPasswordLabel")}
                                </FormLabel>
                                <FormControl>
                                    <PasswordInput
                                        autoComplete="current-password"
                                        {...field}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                    <FormField
                        control={form.control}
                        name="newPassword"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>{t("newPasswordLabel")}</FormLabel>
                                <FormControl>
                                    <PasswordInput
                                        autoComplete="new-password"
                                        {...field}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                    <FormField
                        control={form.control}
                        name="confirmPassword"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>
                                    {t("confirmPasswordLabel")}
                                </FormLabel>
                                <FormControl>
                                    <PasswordInput
                                        autoComplete="new-password"
                                        {...field}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                    <Button
                        type="submit"
                        size="sm"
                        disabled={changePassword.isPending}
                    >
                        {changePassword.isPending
                            ? t("submitPending")
                            : t("submitDefault")}
                    </Button>
                </form>
            </Form>
        </div>
    );
}
