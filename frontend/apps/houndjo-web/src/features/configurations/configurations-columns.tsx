import { format } from "date-fns";
import { DotsHorizontalIcon } from "@radix-ui/react-icons";
import { type ColumnDef } from "@tanstack/react-table";
import { type AppConfiguration } from "@api-client";
import { Pencil, Trash2 } from "lucide-react";
import { cn } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { DataTableColumnHeader } from "@/components/data-table";
import { LongText } from "@/components/long-text";

type BuildConfigurationsColumnsParams = {
    onEdit: (configuration: AppConfiguration) => void;
    onDelete: (configuration: AppConfiguration) => void;
};

function formatDate(value?: string) {
    if (!value) {
        return "N/A";
    }

    return format(new Date(value), "PPp");
}

export function buildConfigurationsColumns({
    onEdit,
    onDelete,
}: BuildConfigurationsColumnsParams): ColumnDef<AppConfiguration>[] {
    return [
        {
            accessorKey: "category",
            header: ({ column }) => (
                <DataTableColumnHeader column={column} title="Category" />
            ),
            cell: ({ row }) => (
                <Badge variant="outline">
                    {row.original.category ?? "N/A"}
                </Badge>
            ),
            enableSorting: false,
            enableHiding: false,
        },
        {
            accessorKey: "code",
            header: ({ column }) => (
                <DataTableColumnHeader column={column} title="Code" />
            ),
            cell: ({ row }) => (
                <LongText className="max-w-32 font-medium">
                    {row.original.code ?? "N/A"}
                </LongText>
            ),
            meta: {
                className: cn(
                    "drop-shadow-[0_1px_2px_rgb(0_0_0_/_0.1)] dark:drop-shadow-[0_1px_2px_rgb(255_255_255_/_0.1)]",
                    "max-md:sticky start-0 bg-background @4xl/content:drop-shadow-none"
                ),
            },
            enableHiding: false,
        },
        {
            accessorKey: "label",
            header: ({ column }) => (
                <DataTableColumnHeader column={column} title="Value" />
            ),
            cell: ({ row }) => (
                <LongText className="max-w-48">
                    {row.original.label ?? "N/A"}
                </LongText>
            ),
            enableSorting: false,
        },
        {
            accessorKey: "description",
            header: ({ column }) => (
                <DataTableColumnHeader column={column} title="Description" />
            ),
            cell: ({ row }) => (
                <LongText className="max-w-64 text-muted-foreground">
                    {row.original.description ?? "No description"}
                </LongText>
            ),
            enableSorting: false,
        },
        {
            accessorKey: "active",
            header: ({ column }) => (
                <DataTableColumnHeader column={column} title="Status" />
            ),
            cell: ({ row }) => (
                <Badge variant={row.original.active ? "default" : "secondary"}>
                    {row.original.active ? "Active" : "Inactive"}
                </Badge>
            ),
            enableSorting: false,
        },
        {
            accessorKey: "lastUpdatedBy",
            header: ({ column }) => (
                <DataTableColumnHeader column={column} title="Updated By" />
            ),
            cell: ({ row }) => row.original.lastUpdatedBy ?? "N/A",
            enableSorting: false,
        },
        {
            accessorKey: "lastUpdateDate",
            header: ({ column }) => (
                <DataTableColumnHeader column={column} title="Updated At" />
            ),
            cell: ({ row }) => formatDate(row.original.lastUpdateDate),
            enableSorting: false,
        },
        {
            id: "actions",
            cell: ({ row }) => (
                <DropdownMenu modal={false}>
                    <DropdownMenuTrigger asChild>
                        <Button
                            variant="ghost"
                            className="flex h-8 w-8 p-0 data-[state=open]:bg-muted"
                        >
                            <DotsHorizontalIcon className="h-4 w-4" />
                            <span className="sr-only">Open menu</span>
                        </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" className="w-[160px]">
                        <DropdownMenuItem onClick={() => onEdit(row.original)}>
                            Edit
                            <Pencil className="ms-auto size-4" />
                        </DropdownMenuItem>
                        <DropdownMenuItem
                            onClick={() => onDelete(row.original)}
                            className="text-red-500!"
                        >
                            Delete
                            <Trash2 className="ms-auto size-4" />
                        </DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu>
            ),
        },
    ];
}
