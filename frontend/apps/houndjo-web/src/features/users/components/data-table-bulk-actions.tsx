import { useState } from "react";
import { type Table } from "@tanstack/react-table";
import { Trash2 } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import {
    Tooltip,
    TooltipContent,
    TooltipTrigger,
} from "@/components/ui/tooltip";
import { DataTableBulkActions as BulkActionsToolbar } from "@/components/data-table";
import { UsersMultiDeleteDialog } from "./users-multi-delete-dialog";

type DataTableBulkActionsProps<TData> = {
    table: Table<TData>;
    canDeleteUsers: boolean;
};

export function DataTableBulkActions<TData>({
    table,
    canDeleteUsers,
}: DataTableBulkActionsProps<TData>) {
    const t = useTranslations("Users.bulkActions");
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const selectedCount = table.getFilteredSelectedRowModel().rows.length;

    if (!canDeleteUsers) {
        return null;
    }

    return (
        <>
            <BulkActionsToolbar
                table={table}
                entityName="user"
                entityLabel={t("entityLabel", { count: selectedCount })}
                selectedWord={t("selectedWord", { count: selectedCount })}
                announcementMessage={t("announcement", {
                    count: selectedCount,
                })}
                toolbarAriaLabel={t("toolbarAriaLabel", {
                    count: selectedCount,
                })}
                selectedCountAriaLabel={t("selectedCountAriaLabel", {
                    count: selectedCount,
                })}
                clearSelectionLabel={t("clearSelection")}
                clearSelectionTitle={t("clearSelectionTitle")}
            >
                <Tooltip>
                    <TooltipTrigger asChild>
                        <Button
                            variant="destructive"
                            size="icon"
                            onClick={() => setShowDeleteConfirm(true)}
                            className="size-8"
                            aria-label={t("deleteSelected")}
                            title={t("deleteSelected")}
                        >
                            <Trash2 />
                            <span className="sr-only">
                                {t("deleteSelected")}
                            </span>
                        </Button>
                    </TooltipTrigger>
                    <TooltipContent>
                        <p>{t("deleteSelected")}</p>
                    </TooltipContent>
                </Tooltip>
            </BulkActionsToolbar>

            <UsersMultiDeleteDialog
                table={table}
                open={showDeleteConfirm}
                onOpenChange={setShowDeleteConfirm}
            />
        </>
    );
}
