"use client";

import { useEffect, useMemo } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import {
    type CreateAdminUserRequestGenderEnumKey,
    type UpdateUserRequestGenderEnumKey,
    getUserAsAdminQueryKey,
    useAssignRoleGroupAsAdmin,
    useCreateUserAsAdmin,
    useGetRoleGroupsAsAdmin,
    useGetUserAsAdmin,
    useRevokeRoleGroupAsAdmin,
    useUpdateUserAsAdmin,
} from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
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
import { ScrollArea } from "@/components/ui/scroll-area";
import { SelectDropdown } from "@/components/select-dropdown";
import { useGenderOptions } from "../data/data";
import {
    mapRoleGroupToOption,
    mapUserDetailsToRow,
    type RoleGroupOption,
    type UserRow,
} from "../data/schema";

function createFormSchema(t: ReturnType<typeof useTranslations>) {
    return z
        .object({
            email: z.string().trim().email(t("emailRequired")),
            firstName: z.string().trim().min(1, t("firstNameRequired")),
            lastName: z.string().trim().min(1, t("lastNameRequired")),
            birthDate: z.string().optional(),
            gender: z.string().optional(),
            phoneNumber: z.string().optional(),
            address: z.string().optional(),
            languageKey: z.string().optional(),
            imageUrl: z.string().optional(),
            roleGroupNames: z.array(z.string()),
            isEdit: z.boolean(),
        })
        .superRefine((values, context) => {
            if (!values.isEdit && values.roleGroupNames.length === 0) {
                context.addIssue({
                    code: z.ZodIssueCode.custom,
                    message: t("roleGroupRequired"),
                    path: ["roleGroupNames"],
                });
            }
        });
}

type UserForm = z.infer<ReturnType<typeof createFormSchema>>;

type UserActionDialogProps = {
    currentRow?: UserRow;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

function getDefaultValues(currentRow?: UserRow): UserForm {
    return {
        email: currentRow?.email ?? "",
        firstName: currentRow?.firstName ?? "",
        lastName: currentRow?.lastName ?? "",
        birthDate: currentRow?.birthDate ?? "",
        gender: currentRow?.gender ?? undefined,
        phoneNumber: currentRow?.phoneNumber ?? "",
        address: currentRow?.address ?? "",
        languageKey: currentRow?.languageKey ?? "",
        imageUrl: currentRow?.imageUrl ?? "",
        roleGroupNames: [],
        isEdit: !!currentRow,
    };
}

function normalizeOptionalString(value?: string) {
    const nextValue = value?.trim();
    return nextValue ? nextValue : undefined;
}

export function UsersActionDialog({
    currentRow,
    open,
    onOpenChange,
}: UserActionDialogProps) {
    const t = useTranslations("Users.form");
    const tValidation = useTranslations("Users.form.validation");
    const genderOptions = useGenderOptions();
    const isEdit = !!currentRow;
    const queryClient = useQueryClient();
    const formSchema = useMemo(
        () => createFormSchema(tValidation),
        [tValidation]
    );
    const form = useForm<UserForm>({
        resolver: zodResolver(formSchema),
        defaultValues: getDefaultValues(currentRow),
    });

    const { data: userDetails } = useGetUserAsAdmin(
        currentRow?.id ?? 0,
        undefined,
        {
            query: {
                enabled: open && isEdit && !!currentRow?.id,
            },
        }
    );
    const { data: roleGroupsData, isLoading: isRoleGroupsLoading } =
        useGetRoleGroupsAsAdmin(
            {
                pageable: {
                    page: 0,
                    size: 100,
                },
            },
            undefined,
            {
                query: {
                    enabled: open,
                },
            }
        );

    const roleGroupOptions = useMemo(
        () =>
            (roleGroupsData?.items ?? [])
                .map(mapRoleGroupToOption)
                .filter((option): option is RoleGroupOption => option !== null)
                .sort((left, right) => left.name.localeCompare(right.name)),
        [roleGroupsData?.items]
    );

    useEffect(() => {
        if (isEdit && userDetails) {
            const existingRoleGroupNames = (userDetails.roleGroups ?? [])
                .map((rg) => rg.name)
                .filter((name): name is string => !!name);
            form.reset({
                ...getDefaultValues(mapUserDetailsToRow(userDetails)),
                roleGroupNames: existingRoleGroupNames,
                isEdit: true,
            });
            return;
        }

        form.reset(getDefaultValues(currentRow));
    }, [currentRow, form, isEdit, userDetails, open]);

    const invalidateUsers = async () => {
        // getUsersAsAdminQueryKey() requires a filter/pageable params
        // argument, so invalidate by the shared URL prefix instead of
        // reconstructing whatever params the list view happened to use.
        await queryClient.invalidateQueries({
            queryKey: [{ url: "/api/admin/users" }],
        });
    };

    const { mutate: createUser, isPending: isCreatingUser } =
        useCreateUserAsAdmin({
            mutation: {
                onSuccess: async () => {
                    await invalidateUsers();
                    toast.success(t("toastCreated"));
                    form.reset(getDefaultValues());
                    onOpenChange(false);
                },
                onError: handleServerError,
            },
        });

    const { mutateAsync: updateUserAsync, isPending: isUpdatingUser } =
        useUpdateUserAsAdmin();

    const { mutateAsync: assignRoleGroupAsync, isPending: isAssigning } =
        useAssignRoleGroupAsAdmin();

    const { mutateAsync: revokeRoleGroupAsync, isPending: isRevoking } =
        useRevokeRoleGroupAsAdmin();

    const isPending =
        isCreatingUser || isUpdatingUser || isAssigning || isRevoking;

    const onSubmit = async (values: UserForm) => {
        if (isEdit && currentRow?.id) {
            const userId = currentRow.id;
            const initialIds = new Set(
                (userDetails?.roleGroups ?? [])
                    .map((rg) => rg.id)
                    .filter((id): id is number => !!id)
            );
            const targetIds = new Set(
                roleGroupOptions
                    .filter((opt) => values.roleGroupNames.includes(opt.name))
                    .map((opt) => opt.id)
            );
            const toAssign = [...targetIds].filter((id) => !initialIds.has(id));
            const toRevoke = [...initialIds].filter((id) => !targetIds.has(id));

            try {
                const [updatedUser] = await Promise.all([
                    updateUserAsync({
                        id: userId,
                        data: {
                            firstName: values.firstName.trim(),
                            lastName: values.lastName.trim(),
                            birthDate: normalizeOptionalString(
                                values.birthDate
                            ),
                            gender: values.gender
                                ? (values.gender as UpdateUserRequestGenderEnumKey)
                                : undefined,
                            phoneNumber: normalizeOptionalString(
                                values.phoneNumber
                            ),
                            address: normalizeOptionalString(values.address),
                            languageKey: normalizeOptionalString(
                                values.languageKey
                            ),
                            imageUrl: normalizeOptionalString(values.imageUrl),
                        },
                    }),
                    ...toAssign.map((roleGroupId) =>
                        assignRoleGroupAsync({
                            id: userId,
                            data: { roleGroupId },
                        })
                    ),
                    ...toRevoke.map((roleGroupId) =>
                        revokeRoleGroupAsync({ id: userId, roleGroupId })
                    ),
                ]);
                await invalidateUsers();
                queryClient.setQueryData(
                    getUserAsAdminQueryKey(userId),
                    updatedUser
                );
                toast.success(t("toastUpdated"));
                onOpenChange(false);
            } catch (error) {
                handleServerError(error);
            }
            return;
        }

        createUser({
            data: {
                email: values.email.trim(),
                firstName: values.firstName.trim(),
                lastName: values.lastName.trim(),
                birthDate: normalizeOptionalString(values.birthDate),
                gender: values.gender
                    ? (values.gender as CreateAdminUserRequestGenderEnumKey)
                    : undefined,
                phoneNumber: normalizeOptionalString(values.phoneNumber),
                address: normalizeOptionalString(values.address),
                languageKey: normalizeOptionalString(values.languageKey),
                imageUrl: normalizeOptionalString(values.imageUrl),
                roleGroupNames: values.roleGroupNames,
            },
        });
    };

    return (
        <Dialog
            open={open}
            onOpenChange={(nextOpen) => {
                if (!isPending) {
                    if (!nextOpen) {
                        form.reset(getDefaultValues(currentRow));
                    }
                    onOpenChange(nextOpen);
                }
            }}
        >
            <DialogContent className="flex max-h-[90vh] flex-col overflow-y-auto sm:max-w-2xl sm:overflow-hidden">
                <DialogHeader className="text-start">
                    <DialogTitle>
                        {isEdit ? t("editTitle") : t("addTitle")}
                    </DialogTitle>
                    <DialogDescription>
                        {isEdit
                            ? t("editDescription")
                            : t("addDescription")}
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form
                        id="user-form"
                        onSubmit={form.handleSubmit(onSubmit)}
                        className="flex min-h-0 flex-1 flex-col space-y-4"
                    >
                        <ScrollArea className="pe-4 sm:min-h-0 sm:flex-1">
                            <div className="space-y-4 px-1">
                                <FormField
                                    control={form.control}
                                    name="email"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                {t("fields.email")}
                                            </FormLabel>
                                            <FormControl>
                                                <Input
                                                    {...field}
                                                    disabled={
                                                        isEdit || isPending
                                                    }
                                                    placeholder={t(
                                                        "fields.emailPlaceholder"
                                                    )}
                                                />
                                            </FormControl>
                                            {isEdit && (
                                                <FormDescription>
                                                    {t(
                                                        "fields.emailEditNote"
                                                    )}
                                                </FormDescription>
                                            )}
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <FormField
                                        control={form.control}
                                        name="firstName"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>
                                                    {t("fields.firstName")}
                                                </FormLabel>
                                                <FormControl>
                                                    <Input
                                                        {...field}
                                                        disabled={isPending}
                                                        placeholder={t(
                                                            "fields.firstNamePlaceholder"
                                                        )}
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
                                                <FormLabel>
                                                    {t("fields.lastName")}
                                                </FormLabel>
                                                <FormControl>
                                                    <Input
                                                        {...field}
                                                        disabled={isPending}
                                                        placeholder={t(
                                                            "fields.lastNamePlaceholder"
                                                        )}
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </div>
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <FormField
                                        control={form.control}
                                        name="birthDate"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>
                                                    {t("fields.birthDate")}
                                                </FormLabel>
                                                <FormControl>
                                                    <Input
                                                        {...field}
                                                        type="date"
                                                        disabled={isPending}
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                    <FormField
                                        control={form.control}
                                        name="gender"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>
                                                    {t("fields.gender")}
                                                </FormLabel>
                                                <SelectDropdown
                                                    defaultValue={field.value}
                                                    onValueChange={
                                                        field.onChange
                                                    }
                                                    placeholder={t(
                                                        "fields.genderPlaceholder"
                                                    )}
                                                    items={genderOptions}
                                                    disabled={isPending}
                                                    className="w-full"
                                                    isControlled
                                                />
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </div>
                                <FormField
                                    control={form.control}
                                    name="phoneNumber"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                {t("fields.phoneNumber")}
                                            </FormLabel>
                                            <FormControl>
                                                <Input
                                                    {...field}
                                                    disabled={isPending}
                                                    placeholder={t(
                                                        "fields.phoneNumberPlaceholder"
                                                    )}
                                                />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="address"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                {t("fields.address")}
                                            </FormLabel>
                                            <FormControl>
                                                <Input
                                                    {...field}
                                                    disabled={isPending}
                                                    placeholder={t(
                                                        "fields.addressPlaceholder"
                                                    )}
                                                />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <FormField
                                        control={form.control}
                                        name="languageKey"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>
                                                    {t("fields.languageKey")}
                                                </FormLabel>
                                                <FormControl>
                                                    <Input
                                                        {...field}
                                                        disabled={isPending}
                                                        placeholder={t(
                                                            "fields.languageKeyPlaceholder"
                                                        )}
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                    <FormField
                                        control={form.control}
                                        name="imageUrl"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>
                                                    {t("fields.imageUrl")}
                                                </FormLabel>
                                                <FormControl>
                                                    <Input
                                                        {...field}
                                                        disabled={isPending}
                                                        placeholder={t(
                                                            "fields.imageUrlPlaceholder"
                                                        )}
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </div>
                                <FormField
                                    control={form.control}
                                    name="roleGroupNames"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                {t("fields.roleGroups")}
                                            </FormLabel>
                                            <div className="rounded-md border">
                                                <ScrollArea className="h-48">
                                                    <div className="space-y-3 p-4">
                                                        {isRoleGroupsLoading ? (
                                                            <p className="text-sm text-muted-foreground">
                                                                {t(
                                                                    "roleGroupsLoading"
                                                                )}
                                                            </p>
                                                        ) : roleGroupOptions.length ===
                                                          0 ? (
                                                            <p className="text-sm text-muted-foreground">
                                                                {t(
                                                                    "roleGroupsEmpty"
                                                                )}
                                                            </p>
                                                        ) : (
                                                            roleGroupOptions.map(
                                                                (roleGroup) => {
                                                                    const checked =
                                                                        field.value.includes(
                                                                            roleGroup.name
                                                                        );

                                                                    return (
                                                                        <label
                                                                            key={
                                                                                roleGroup.id
                                                                            }
                                                                            className="flex items-start gap-3"
                                                                        >
                                                                            <Checkbox
                                                                                checked={
                                                                                    checked
                                                                                }
                                                                                onCheckedChange={(
                                                                                    nextChecked
                                                                                ) => {
                                                                                    const nextValues =
                                                                                        nextChecked
                                                                                            ? [
                                                                                                  ...field.value,
                                                                                                  roleGroup.name,
                                                                                              ]
                                                                                            : field.value.filter(
                                                                                                  (
                                                                                                      value
                                                                                                  ) =>
                                                                                                      value !==
                                                                                                      roleGroup.name
                                                                                              );

                                                                                    field.onChange(
                                                                                        nextValues
                                                                                    );
                                                                                }}
                                                                            />
                                                                            <div className="space-y-1">
                                                                                <p className="text-sm leading-none font-medium">
                                                                                    {
                                                                                        roleGroup.name
                                                                                    }
                                                                                </p>
                                                                                {roleGroup.description && (
                                                                                    <p className="text-sm text-muted-foreground">
                                                                                        {
                                                                                            roleGroup.description
                                                                                        }
                                                                                    </p>
                                                                                )}
                                                                            </div>
                                                                        </label>
                                                                    );
                                                                }
                                                            )
                                                        )}
                                                    </div>
                                                </ScrollArea>
                                            </div>
                                            <FormDescription>
                                                {isEdit
                                                    ? t(
                                                          "roleGroupsDescriptionEdit"
                                                      )
                                                    : t(
                                                          "roleGroupsDescriptionAdd"
                                                      )}
                                            </FormDescription>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>
                        </ScrollArea>
                        <DialogFooter className="mt-2 border-t pt-4">
                            <Button
                                type="submit"
                                form="user-form"
                                disabled={isPending}
                                className="w-full sm:w-auto"
                            >
                                {isEdit ? t("submitEdit") : t("submitAdd")}
                            </Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}
