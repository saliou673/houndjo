"use client";

import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useDeleteUserAsAdmin } from "@api-client";
import { AlertTriangle } from "lucide-react";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { type UserRow } from "../data/schema";

type UserDeleteDialogProps = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    currentRow: UserRow;
};

export function UsersDeleteDialog({
    open,
    onOpenChange,
    currentRow,
}: UserDeleteDialogProps) {
    const t = useTranslations("Users.deleteDialog");
    const [value, setValue] = useState("");
    const queryClient = useQueryClient();
    const { mutate, isPending } = useDeleteUserAsAdmin({
        mutation: {
            onSuccess: async () => {
                // getUsersAsAdminQueryKey() requires a filter/pageable params
                // argument, so invalidate by the shared URL prefix instead of
                // reconstructing whatever params the list view happened to use.
                await queryClient.invalidateQueries({
                    queryKey: [{ url: "/api/admin/users" }],
                });
                toast.success(t("successToast"));
                setValue("");
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const handleDelete = () => {
        if (value.trim() !== currentRow.email) {
            return;
        }

        mutate({ id: currentRow.id });
    };

    return (
        <ConfirmDialog
            open={open}
            onOpenChange={onOpenChange}
            handleConfirm={handleDelete}
            disabled={value.trim() !== currentRow.email}
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
                <div className="space-y-4">
                    <p className="mb-2">
                        {t.rich("confirmMessage", {
                            email: currentRow.email,
                            bold: (chunks) => (
                                <span className="font-bold">{chunks}</span>
                            ),
                        })}
                    </p>

                    <Label className="my-2">
                        {t("emailLabel")}
                        <Input
                            value={value}
                            onChange={(e) => setValue(e.target.value)}
                            placeholder={t("emailPlaceholder")}
                        />
                    </Label>

                    <Alert variant="destructive">
                        <AlertTitle>{t("warningTitle")}</AlertTitle>
                        <AlertDescription>
                            {t("warningDescription")}
                        </AlertDescription>
                    </Alert>
                </div>
            }
            confirmText={t("confirmButton")}
            destructive
        />
    );
}
