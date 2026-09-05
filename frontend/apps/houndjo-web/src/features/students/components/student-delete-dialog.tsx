"use client";

import { useQueryClient } from "@tanstack/react-query";
import { useDeleteStudent } from "@api-client";
import { AlertTriangle } from "lucide-react";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { type StudentRow } from "../data/schema";

type StudentDeleteDialogProps = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    currentRow: StudentRow;
};

export function StudentDeleteDialog({
    open,
    onOpenChange,
    currentRow,
}: StudentDeleteDialogProps) {
    const t = useTranslations("Students.deleteDialog");
    const queryClient = useQueryClient();
    const { mutate, isPending } = useDeleteStudent({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: [{ url: "/api/v1/students" }],
                });
                toast.success(t("successToast"));
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    return (
        <ConfirmDialog
            open={open}
            onOpenChange={onOpenChange}
            handleConfirm={() => mutate({ id: currentRow.id })}
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
                        name: `${currentRow.firstName} ${currentRow.lastName}`,
                        bold: (chunks) => <span className="font-bold">{chunks}</span>,
                    })}
                </p>
            }
            confirmText={t("confirmButton")}
            destructive
        />
    );
}
