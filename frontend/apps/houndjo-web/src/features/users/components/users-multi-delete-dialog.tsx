"use client";

import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { type Table } from "@tanstack/react-table";
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

type UserMultiDeleteDialogProps<TData> = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    table: Table<TData>;
};

const CONFIRM_WORD = "DELETE";

export function UsersMultiDeleteDialog<TData>({
    open,
    onOpenChange,
    table,
}: UserMultiDeleteDialogProps<TData>) {
    const t = useTranslations("Users.multiDeleteDialog");
    const tDelete = useTranslations("Users.deleteDialog");
    const [value, setValue] = useState("");
    const queryClient = useQueryClient();
    const { mutateAsync, isPending } = useDeleteUserAsAdmin();

    const selectedRows = table.getFilteredSelectedRowModel().rows;

    const handleDelete = async () => {
        if (value.trim() !== CONFIRM_WORD) {
            toast.error(t("typeToConfirmError", { word: CONFIRM_WORD }));
            return;
        }

        let deletedCount = 0;

        for (const row of selectedRows) {
            const user = row.original as UserRow;

            try {
                await mutateAsync({ id: user.id });
                deletedCount += 1;
            } catch (error) {
                handleServerError(error);
            }
        }

        if (deletedCount > 0) {
            // getUsersAsAdminQueryKey() requires a filter/pageable params
            // argument, so invalidate by the shared URL prefix instead of
            // reconstructing whatever params the list view happened to use.
            await queryClient.invalidateQueries({
                queryKey: [{ url: "/api/admin/users" }],
            });
        }

        setValue("");
        onOpenChange(false);

        if (deletedCount === selectedRows.length) {
            table.resetRowSelection();
            toast.success(t("successToast", { count: deletedCount }));
            return;
        }

        toast.error(
            t("partialToast", {
                deleted: deletedCount,
                total: selectedRows.length,
            })
        );
    };

    return (
        <ConfirmDialog
            open={open}
            onOpenChange={onOpenChange}
            handleConfirm={handleDelete}
            disabled={value.trim() !== CONFIRM_WORD}
            isLoading={isPending}
            title={
                <span className="text-destructive">
                    <AlertTriangle
                        className="me-1 inline-block stroke-destructive"
                        size={18}
                    />{" "}
                    {t("title", { count: selectedRows.length })}
                </span>
            }
            desc={
                <div className="space-y-4">
                    <p className="mb-2">
                        {t("description")}
                        <br />
                    </p>

                    <Label className="my-4 flex flex-col items-start gap-1.5">
                        <span className="">
                            {t("confirmLabel", { word: CONFIRM_WORD })}
                        </span>
                        <Input
                            value={value}
                            onChange={(e) => setValue(e.target.value)}
                            placeholder={t("placeholder", {
                                word: CONFIRM_WORD,
                            })}
                        />
                    </Label>

                    <Alert variant="destructive">
                        <AlertTitle>{tDelete("warningTitle")}</AlertTitle>
                        <AlertDescription>
                            {tDelete("warningDescription")}
                        </AlertDescription>
                    </Alert>
                </div>
            }
            confirmText={tDelete("confirmButton")}
            destructive
        />
    );
}
