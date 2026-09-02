"use client";

import { useEffect, useMemo } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import {
    useCreateRoleGroupAsAdmin,
    useGetPermissionsAsAdmin,
    useUpdateRoleGroupAsAdmin,
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
import { Textarea } from "@/components/ui/textarea";
import { type RoleGroupRow } from "../data/schema";

function createFormSchema(t: ReturnType<typeof useTranslations>) {
    return z.object({
        name: z.string().trim().min(1, t("nameRequired")).max(100),
        description: z.string().optional(),
        permissionCodes: z
            .array(z.string())
            .min(1, t("permissionRequired")),
    });
}

type RoleGroupForm = z.infer<ReturnType<typeof createFormSchema>>;

type RoleGroupActionDialogProps = {
    currentRow?: RoleGroupRow;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

function getDefaultValues(currentRow?: RoleGroupRow): RoleGroupForm {
    return {
        name: currentRow?.name ?? "",
        description: currentRow?.description ?? "",
        permissionCodes: currentRow?.permissionCodes ?? [],
    };
}

export function RoleGroupsActionDialog({
    currentRow,
    open,
    onOpenChange,
}: RoleGroupActionDialogProps) {
    const t = useTranslations("RoleGroups.form");
    const tValidation = useTranslations("RoleGroups.form.validation");
    const isEdit = !!currentRow;
    const queryClient = useQueryClient();
    const formSchema = useMemo(
        () => createFormSchema(tValidation),
        [tValidation]
    );

    const form = useForm<RoleGroupForm>({
        resolver: zodResolver(formSchema),
        defaultValues: getDefaultValues(currentRow),
    });

    const { data: permissionsData, isLoading: isPermissionsLoading } =
        useGetPermissionsAsAdmin(
            { pageable: { page: 0, size: 1000 } },
            undefined,
            { query: { enabled: open } }
        );

    const permissionOptions = useMemo(
        () =>
            (permissionsData?.items ?? [])
                .filter((p): p is typeof p & { code: string } => !!p.code)
                .sort((a, b) => a.code.localeCompare(b.code)),
        [permissionsData?.items]
    );

    useEffect(() => {
        if (open) {
            form.reset(getDefaultValues(currentRow));
        }
    }, [currentRow, form, open]);

    const invalidateRoleGroups = async () => {
        // getRoleGroupsAsAdminQueryKey() requires a pageable params argument,
        // so invalidate by the shared URL prefix instead of reconstructing
        // whatever params the list view happened to use.
        await queryClient.invalidateQueries({
            queryKey: [{ url: "/api/admin/role-groups" }],
        });
    };

    const { mutate: createRoleGroup, isPending: isCreating } =
        useCreateRoleGroupAsAdmin({
            mutation: {
                onSuccess: async () => {
                    await invalidateRoleGroups();
                    toast.success(t("toastCreated"));
                    form.reset(getDefaultValues());
                    onOpenChange(false);
                },
                onError: handleServerError,
            },
        });

    const { mutate: updateRoleGroup, isPending: isUpdating } =
        useUpdateRoleGroupAsAdmin({
            mutation: {
                onSuccess: async () => {
                    await invalidateRoleGroups();
                    toast.success(t("toastUpdated"));
                    onOpenChange(false);
                },
                onError: handleServerError,
            },
        });

    const isPending = isCreating || isUpdating;

    const onSubmit = (values: RoleGroupForm) => {
        const description = values.description?.trim() ?? "";

        if (isEdit && currentRow?.id) {
            updateRoleGroup({
                id: currentRow.id,
                data: {
                    name: values.name.trim(),
                    description,
                    permissionCodes: values.permissionCodes,
                },
            });
            return;
        }

        createRoleGroup({
            data: {
                name: values.name.trim(),
                description,
                permissionCodes: values.permissionCodes,
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
            <DialogContent className="flex max-h-[90vh] flex-col overflow-y-auto sm:max-w-lg sm:overflow-hidden">
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
                        id="role-group-form"
                        onSubmit={form.handleSubmit(onSubmit)}
                        className="flex min-h-0 flex-1 flex-col space-y-4"
                    >
                        <ScrollArea className="pe-4 sm:min-h-0 sm:flex-1">
                            <div className="space-y-4 px-1">
                                <FormField
                                    control={form.control}
                                    name="name"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                {t("fields.name")}
                                            </FormLabel>
                                            <FormControl>
                                                <Input
                                                    {...field}
                                                    disabled={isPending}
                                                    placeholder={t(
                                                        "fields.namePlaceholder"
                                                    )}
                                                />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="description"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                {t("fields.description")}
                                            </FormLabel>
                                            <FormControl>
                                                <Textarea
                                                    {...field}
                                                    disabled={isPending}
                                                    placeholder={t(
                                                        "fields.descriptionPlaceholder"
                                                    )}
                                                    rows={3}
                                                />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="permissionCodes"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>
                                                {t("fields.permissions")}
                                            </FormLabel>
                                            <div className="rounded-md border">
                                                <ScrollArea className="h-56">
                                                    <div className="space-y-3 p-4">
                                                        {isPermissionsLoading ? (
                                                            <p className="text-sm text-muted-foreground">
                                                                {t(
                                                                    "permissionsLoading"
                                                                )}
                                                            </p>
                                                        ) : permissionOptions.length ===
                                                          0 ? (
                                                            <p className="text-sm text-muted-foreground">
                                                                {t(
                                                                    "permissionsEmpty"
                                                                )}
                                                            </p>
                                                        ) : (
                                                            permissionOptions.map(
                                                                (
                                                                    permission
                                                                ) => {
                                                                    const checked =
                                                                        field.value.includes(
                                                                            permission.code
                                                                        );

                                                                    return (
                                                                        <label
                                                                            key={
                                                                                permission.code
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
                                                                                                  permission.code,
                                                                                              ]
                                                                                            : field.value.filter(
                                                                                                  (
                                                                                                      v
                                                                                                  ) =>
                                                                                                      v !==
                                                                                                      permission.code
                                                                                              );
                                                                                    field.onChange(
                                                                                        nextValues
                                                                                    );
                                                                                }}
                                                                            />
                                                                            <div className="space-y-1">
                                                                                <p className="font-mono text-sm leading-none font-medium">
                                                                                    {
                                                                                        permission.code
                                                                                    }
                                                                                </p>
                                                                                {permission.description && (
                                                                                    <p className="text-sm text-muted-foreground">
                                                                                        {
                                                                                            permission.description
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
                                                {t("permissionsDescription")}
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
                                form="role-group-form"
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
