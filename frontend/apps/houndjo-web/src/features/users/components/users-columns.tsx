import { type ColumnDef } from "@tanstack/react-table";
import { cn } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { LongText } from "@/components/long-text";
import { userStatusBadgeClassNames } from "../data/data";
import { type UserGender, type UserRow, type UserStatus } from "../data/schema";
import { DataTableRowActions } from "./data-table-row-actions";

type BuildUsersColumnsOptions = {
    canDeleteUsers: boolean;
    canUpdateUsers: boolean;
    genderLabels: Record<UserGender, string>;
    userStatusLabels: Record<UserStatus, string>;
    labels: {
        selectAll: string;
        selectRow: string;
        name: string;
        email: string;
        phoneNumber: string;
        roleGroups: string;
        gender: string;
        status: string;
    };
};

export function buildUsersColumns({
    canDeleteUsers,
    canUpdateUsers,
    genderLabels,
    userStatusLabels,
    labels,
}: BuildUsersColumnsOptions): ColumnDef<UserRow>[] {
    const columns: ColumnDef<UserRow>[] = [];

    if (canDeleteUsers) {
        columns.push({
            id: "select",
            header: ({ table }) => (
                <Checkbox
                    checked={
                        table.getIsAllPageRowsSelected() ||
                        (table.getIsSomePageRowsSelected() && "indeterminate")
                    }
                    onCheckedChange={(value) =>
                        table.toggleAllPageRowsSelected(!!value)
                    }
                    aria-label={labels.selectAll}
                    className="translate-y-[2px]"
                />
            ),
            meta: {
                className: cn(
                    "max-md:sticky start-0 z-10 rounded-tl-[inherit]"
                ),
            },
            cell: ({ row }) => (
                <Checkbox
                    checked={row.getIsSelected()}
                    onCheckedChange={(value) => row.toggleSelected(!!value)}
                    aria-label={labels.selectRow}
                    className="translate-y-[2px]"
                />
            ),
            enableSorting: false,
            enableHiding: false,
        });
    }

    columns.push(
        {
            accessorKey: "fullName",
            header: labels.name,
            cell: ({ row }) => (
                <LongText className="max-w-40 ps-3">
                    {row.original.fullName}
                </LongText>
            ),
            meta: {
                className: cn(
                    "drop-shadow-[0_1px_2px_rgb(0_0_0_/_0.1)] dark:drop-shadow-[0_1px_2px_rgb(255_255_255_/_0.1)]",
                    canDeleteUsers
                        ? "ps-0.5 max-md:sticky start-6 @4xl/content:table-cell @4xl/content:drop-shadow-none"
                        : "ps-0.5"
                ),
            },
            enableHiding: false,
        },
        {
            accessorKey: "email",
            header: labels.email,
            cell: ({ row }) => (
                <LongText className="max-w-52">{row.original.email}</LongText>
            ),
            enableHiding: false,
        },
        {
            accessorKey: "phoneNumber",
            header: labels.phoneNumber,
            cell: ({ row }) => <div>{row.original.phoneNumber ?? "—"}</div>,
            enableSorting: false,
        },
        {
            accessorKey: "roleGroupNames",
            header: labels.roleGroups,
            cell: ({ row }) => (
                <div className="flex flex-wrap gap-1">
                    {row.original.roleGroupNames.map((name) => (
                        <Badge key={name} variant="outline" className="text-xs">
                            {name}
                        </Badge>
                    ))}
                </div>
            ),
            filterFn: (row, _id, value: string[]) =>
                value.some((v) => row.original.roleGroupNames.includes(v)),
            enableSorting: false,
        },
        {
            accessorKey: "gender",
            header: labels.gender,
            cell: ({ row }) => <div>{genderLabels[row.original.gender]}</div>,
            filterFn: (row, id, value) => value.includes(row.getValue(id)),
            enableSorting: false,
        },
        {
            accessorKey: "status",
            header: labels.status,
            cell: ({ row }) => (
                <Badge
                    variant="outline"
                    className={cn(
                        userStatusBadgeClassNames[row.original.status]
                    )}
                >
                    {userStatusLabels[row.original.status]}
                </Badge>
            ),
            filterFn: (row, id, value) => value.includes(row.getValue(id)),
            enableHiding: false,
            enableSorting: false,
        }
    );

    if (canUpdateUsers || canDeleteUsers) {
        columns.push({
            id: "actions",
            cell: ({ row }) => (
                <DataTableRowActions
                    row={row}
                    canUpdateUsers={canUpdateUsers}
                    canDeleteUsers={canDeleteUsers}
                />
            ),
        });
    }

    return columns;
}
