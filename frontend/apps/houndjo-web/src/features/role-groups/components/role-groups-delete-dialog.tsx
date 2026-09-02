"use client";

import { useQueryClient } from "@tanstack/react-query";
import { useDeleteRoleGroupAsAdmin } from "@api-client";
import { AlertTriangle } from "lucide-react";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { type RoleGroupRow } from "../data/schema";

type RoleGroupDeleteDialogProps = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    currentRow: RoleGroupRow;
};

export function RoleGroupsDeleteDialog({
    open,
    onOpenChange,
    currentRow,
}: RoleGroupDeleteDialogProps) {
    const t = useTranslations("RoleGroups.deleteDialog");
    const queryClient = useQueryClient();
    const { mutate, isPending } = useDeleteRoleGroupAsAdmin({
        mutation: {
            onSuccess: async () => {
                // getRoleGroupsAsAdminQueryKey() requires a pageable params
                // argument, so invalidate by the shared URL prefix instead of
                // reconstructing whatever params the list view happened to use.
                await queryClient.invalidateQueries({
                    queryKey: [{ url: "/api/admin/role-groups" }],
                });
                toast.success(t("successToast"));
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const handleDelete = () => {
        mutate({ id: currentRow.id });
    };

    return (
        <ConfirmDialog
            open={open}
            onOpenChange={onOpenChange}
            handleConfirm={handleDelete}
            isLoading={isPending}
            title={
                <span className="text-destructive">
                    <AlertTriangle
                        className="me-1 inline-block stroke-destructive"
                        size={18}
                    />{" "}
                    {t("title")}
                </span>
            }
            desc={
                <p>
                    {t.rich("confirmMessage", {
                        name: currentRow.name,
                        bold: (chunks) => (
                            <span className="font-bold">{chunks}</span>
                        ),
                    })}
                </p>
            }
            confirmText={t("confirmButton")}
            destructive
        />
    );
}
