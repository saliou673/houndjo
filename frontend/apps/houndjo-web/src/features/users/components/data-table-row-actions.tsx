import { DotsHorizontalIcon } from "@radix-ui/react-icons";
import { type Row } from "@tanstack/react-table";
import { Trash2, UserPen } from "lucide-react";
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
import { type UserRow } from "../data/schema";
import { useUsers } from "./users-provider";

type DataTableRowActionsProps = {
    row: Row<UserRow>;
    canUpdateUsers: boolean;
    canDeleteUsers: boolean;
};

export function DataTableRowActions({
    row,
    canUpdateUsers,
    canDeleteUsers,
}: DataTableRowActionsProps) {
    const t = useTranslations("Users.rowActions");
    const { setOpen, setCurrentRow } = useUsers();

    if (!canUpdateUsers && !canDeleteUsers) {
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
        <>
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
                    {canUpdateUsers && (
                        <DropdownMenuItem onClick={handleEdit}>
                            {t("edit")}
                            <DropdownMenuShortcut>
                                <UserPen size={16} />
                            </DropdownMenuShortcut>
                        </DropdownMenuItem>
                    )}
                    {canUpdateUsers && canDeleteUsers && (
                        <DropdownMenuSeparator />
                    )}
                    {canDeleteUsers && (
                        <DropdownMenuItem
                            onClick={handleDelete}
                            className="text-red-500!"
                        >
                            {t("delete")}
                            <DropdownMenuShortcut>
                                <Trash2 size={16} />
                            </DropdownMenuShortcut>
                        </DropdownMenuItem>
                    )}
                </DropdownMenuContent>
            </DropdownMenu>
        </>
    );
}
