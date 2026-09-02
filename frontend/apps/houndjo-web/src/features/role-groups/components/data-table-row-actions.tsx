import { DotsHorizontalIcon } from "@radix-ui/react-icons";
import { type Row } from "@tanstack/react-table";
import { Pencil, Trash2 } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuShortcut,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { type RoleGroupRow } from "../data/schema";
import { useRoleGroups } from "./role-groups-provider";

type DataTableRowActionsProps = {
    row: Row<RoleGroupRow>;
    canManageRoleGroups: boolean;
};

export function DataTableRowActions({
    row,
    canManageRoleGroups,
}: DataTableRowActionsProps) {
    const t = useTranslations("RoleGroups.rowActions");
    const { setOpen, setCurrentRow } = useRoleGroups();

    if (!canManageRoleGroups) {
        return null;
    }

    const handleEdit = () => {
        setCurrentRow(row.original);
        setOpen("edit");
    };

    const handleDelete = () => {
        setCurrentRow(row.original);
        setOpen("delete");
    };

    return (
        <DropdownMenu modal={false}>
            <DropdownMenuTrigger asChild>
                <Button
                    variant="ghost"
                    className="flex h-8 w-8 p-0 data-[state=open]:bg-muted"
                >
                    <DotsHorizontalIcon className="h-4 w-4" />
                    <span className="sr-only">{t("openMenu")}</span>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-[160px]">
                <DropdownMenuItem onClick={handleEdit}>
                    {t("edit")}
                    <DropdownMenuShortcut>
                        <Pencil size={16} />
                    </DropdownMenuShortcut>
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                    onClick={handleDelete}
                    className="text-red-500!"
                >
                    {t("delete")}
                    <DropdownMenuShortcut>
                        <Trash2 size={16} />
                    </DropdownMenuShortcut>
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
