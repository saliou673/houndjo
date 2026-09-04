"use client";

import { useQueryClient } from "@tanstack/react-query";
import { useEndEnrollment } from "@api-client";
import { AlertTriangle } from "lucide-react";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { type EnrollmentRow } from "../data/schema";

type EnrollmentEndDialogProps = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    currentRow: EnrollmentRow;
};

export function EnrollmentEndDialog({
    open,
    onOpenChange,
    currentRow,
}: EnrollmentEndDialogProps) {
    const t = useTranslations("Enrollments.endDialog");
    const queryClient = useQueryClient();
    const { mutate, isPending } = useEndEnrollment({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: [{ url: "/api/v1/enrollments" }],
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
                        name: currentRow.studentName,
                        className: currentRow.className,
                        bold: (chunks) => <span className="font-bold">{chunks}</span>,
                    })}
                </p>
            }
            confirmText={t("confirmButton")}
            destructive
        />
    );
}
