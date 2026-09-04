"use client";

import { useQueryClient } from "@tanstack/react-query";
import { useDeleteCourse } from "@api-client";
import { AlertTriangle } from "lucide-react";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { type CourseRow } from "../data/schema";

type CourseDeleteDialogProps = {
    classId: number;
    open: boolean;
    onOpenChange: (open: boolean) => void;
    currentRow: CourseRow;
};

export function CourseDeleteDialog({
    classId,
    open,
    onOpenChange,
    currentRow,
}: CourseDeleteDialogProps) {
    const t = useTranslations("Classes.courseDeleteDialog");
    const queryClient = useQueryClient();
    const { mutate, isPending } = useDeleteCourse({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: [
                        { url: "/api/v1/classes/:classId/courses", params: { classId } },
                    ],
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
            handleConfirm={() => mutate({ classId, id: currentRow.id })}
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
                        bold: (chunks) => <span className="font-bold">{chunks}</span>,
                    })}
                </p>
            }
            confirmText={t("confirmButton")}
            destructive
        />
    );
}
