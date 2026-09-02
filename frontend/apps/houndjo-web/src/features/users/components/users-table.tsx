import { useEffect, useMemo, useState } from "react";
import {
    type ColumnFiltersState,
    type VisibilityState,
    flexRender,
    getCoreRowModel,
    useReactTable,
} from "@tanstack/react-table";
import { useTranslations } from "next-intl";
import {
    useGetUsersAsAdmin,
    useGetRoleGroupsAsAdmin,
    type UserFilter,
    type UserGenderFilterInEnumKey,
    type UserStatusFilterInEnumKey,
} from "@api-client";
import { cn } from "@/lib/utils";
import {
    useNextNavigateSearch,
    useNextSearchObject,
} from "@/hooks/use-next-search-state";
import { useTableUrlState } from "@/hooks/use-table-url-state";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { DataTablePagination, DataTableToolbar } from "@/components/data-table";
import {
    useGenderLabels,
    useGenderOptions,
    useUserStatusLabels,
    useUserStatusOptions,
} from "../data/data";
import { mapUserDetailsToRow } from "../data/schema";
import { DataTableBulkActions } from "./data-table-bulk-actions";
import { buildUsersColumns } from "./users-columns";

type DataTableProps = {
    canDeleteUsers: boolean;
    canUpdateUsers: boolean;
};

function toStringArray(value: unknown): string[] {
    if (Array.isArray(value)) {
        return value.filter(
            (item): item is string =>
                typeof item === "string" && item.length > 0
        );
    }

    if (typeof value === "string" && value.length > 0) {
        return [value];
    }

    return [];
}

function getArrayFilterValue(
    columnFilters: { id: string; value: unknown }[],
    columnId: string
) {
    const filter = columnFilters.find((item) => item.id === columnId);

    if (!filter) {
        return [];
    }

    return toStringArray(filter.value);
}

function getStringFilterValue(
    columnFilters: { id: string; value: unknown }[],
    columnId: string
) {
    const filter = columnFilters.find((item) => item.id === columnId);

    if (!filter || typeof filter.value !== "string") {
        return "";
    }

    return filter.value;
}

export function UsersTable({ canDeleteUsers, canUpdateUsers }: DataTableProps) {
    const t = useTranslations("Users.table");
    const genderLabels = useGenderLabels();
    const genderOptions = useGenderOptions();
    const userStatusLabels = useUserStatusLabels();
    const userStatusOptions = useUserStatusOptions();
    const search = useNextSearchObject();
    const navigate = useNextNavigateSearch();
    const [rowSelection, setRowSelection] = useState({});
    const [columnVisibility, setColumnVisibility] = useState<VisibilityState>(
        {}
    );

    const {
        columnFilters,
        onColumnFiltersChange,
        pagination,
        onPaginationChange,
        ensurePageInRange,
    } = useTableUrlState({
        search,
        navigate,
        pagination: { defaultPage: 1, defaultPageSize: 10 },
        globalFilter: { enabled: false },
        columnFilters: [
            { columnId: "email", searchKey: "email", type: "string" },
            { columnId: "status", searchKey: "status", type: "array" },
            { columnId: "gender", searchKey: "gender", type: "array" },
            {
                columnId: "roleGroupNames",
                searchKey: "roleGroup",
                type: "array",
            },
        ],
    });
    const { data: roleGroupsData } = useGetRoleGroupsAsAdmin(
        { pageable: { page: 0, size: 100 } },
        undefined,
        { query: { enabled: true } }
    );
    const roleGroupOptions = useMemo(
        () =>
            (roleGroupsData?.items ?? [])
                .filter(
                    (rg): rg is typeof rg & { id: number; name: string } =>
                        !!rg.id && !!rg.name
                )
                .map((rg) => ({ label: rg.name, value: rg.name }))
                .sort((a, b) => a.label.localeCompare(b.label)),
        [roleGroupsData?.items]
    );

    const emailFilterValue = useMemo(
        () => getStringFilterValue(columnFilters, "email"),
        [columnFilters]
    );
    const [emailSearchValue, setEmailSearchValue] = useState(emailFilterValue);

    useEffect(() => {
        setEmailSearchValue(emailFilterValue);
    }, [emailFilterValue]);

    useEffect(() => {
        const trimmedSearchValue = emailSearchValue.trim();
        const trimmedFilterValue = emailFilterValue.trim();

        if (trimmedSearchValue === trimmedFilterValue) {
            return;
        }

        const timeout = window.setTimeout(() => {
            onColumnFiltersChange((previousFilters) => {
                const nextFilters = previousFilters.filter(
                    (filter) => filter.id !== "email"
                );

                if (!trimmedSearchValue) {
                    return nextFilters;
                }

                return [
                    ...nextFilters,
                    {
                        id: "email",
                        value: trimmedSearchValue,
                    },
                ] satisfies ColumnFiltersState;
            });
        }, 300);

        return () => window.clearTimeout(timeout);
    }, [emailFilterValue, emailSearchValue, onColumnFiltersChange]);

    const filter = useMemo<UserFilter>(() => {
        const emailValues = getArrayFilterValue(columnFilters, "email");
        const statusValues = getArrayFilterValue(columnFilters, "status");
        const genderValues = getArrayFilterValue(columnFilters, "gender");
        const roleGroupValues = getArrayFilterValue(
            columnFilters,
            "roleGroupNames"
        );
        const nextFilter: UserFilter = {};
        const email = emailValues[0]?.trim();

        if (email) {
            nextFilter.email = { contains: email };
        }

        if (statusValues.length === 1) {
            nextFilter.status = {
                equals: statusValues[0] as UserStatusFilterInEnumKey,
            };
        } else if (statusValues.length > 1) {
            nextFilter.status = {
                in: statusValues as UserStatusFilterInEnumKey[],
            };
        }

        if (genderValues.length === 1) {
            nextFilter.gender = {
                equals: genderValues[0] as UserGenderFilterInEnumKey,
            };
        } else if (genderValues.length > 1) {
            nextFilter.gender = {
                in: genderValues as UserGenderFilterInEnumKey[],
            };
        }

        if (roleGroupValues.length === 1) {
            nextFilter.roleGroupName = { equals: roleGroupValues[0] };
        } else if (roleGroupValues.length > 1) {
            nextFilter.roleGroupName = { in: roleGroupValues };
        }

        return nextFilter;
    }, [columnFilters]);

    const usersQueryParams = useMemo(
        () => ({
            filter,
            pageable: {
                page: pagination.pageIndex,
                size: pagination.pageSize,
            },
        }),
        [filter, pagination.pageIndex, pagination.pageSize]
    );

    useEffect(() => {
        setRowSelection({});
    }, [pagination.pageIndex, pagination.pageSize, filter]);

    const { data, isLoading, isFetching, isError, error } = useGetUsersAsAdmin(
        usersQueryParams,
        undefined,
        {
            query: {
                placeholderData: (previousData) => previousData,
            },
        }
    );

    const rows = useMemo(
        () => (data?.items ?? []).map(mapUserDetailsToRow),
        [data?.items]
    );
    const totalPages = data?.totalPages ?? 0;
    const columns = useMemo(
        () =>
            buildUsersColumns({
                canDeleteUsers,
                canUpdateUsers,
                genderLabels,
                userStatusLabels,
                labels: {
                    selectAll: t("selectAll"),
                    selectRow: t("selectRow"),
                    name: t("columns.name"),
                    email: t("columns.email"),
                    phoneNumber: t("columns.phoneNumber"),
                    roleGroups: t("columns.roleGroups"),
                    gender: t("columns.gender"),
                    status: t("columns.status"),
                },
            }),
        [canDeleteUsers, canUpdateUsers, genderLabels, userStatusLabels, t]
    );

    // eslint-disable-next-line react-hooks/incompatible-library
    const table = useReactTable({
        data: rows,
        columns,
        pageCount: totalPages,
        state: {
            pagination,
            rowSelection,
            columnFilters,
            columnVisibility,
        },
        enableRowSelection: canDeleteUsers,
        manualPagination: true,
        manualFiltering: true,
        getRowId: (row) => String(row.id),
        onPaginationChange,
        onColumnFiltersChange,
        onRowSelectionChange: setRowSelection,
        onColumnVisibilityChange: setColumnVisibility,
        getCoreRowModel: getCoreRowModel(),
    });

    useEffect(() => {
        ensurePageInRange(totalPages);
    }, [ensurePageInRange, totalPages]);

    return (
        <div
            className={cn(
                'max-sm:has-[div[role="toolbar"]]:mb-16', // Add margin bottom to the table on mobile when the toolbar is visible
                "flex flex-1 flex-col gap-4"
            )}
        >
            <DataTableToolbar
                table={table}
                searchPlaceholder={t("searchPlaceholder")}
                searchKey="email"
                searchValue={emailSearchValue}
                onSearchChange={setEmailSearchValue}
                filters={[
                    {
                        columnId: "status",
                        title: t("filters.status"),
                        options: userStatusOptions,
                    },
                    {
                        columnId: "gender",
                        title: t("filters.gender"),
                        options: genderOptions,
                    },
                    {
                        columnId: "roleGroupNames",
                        title: t("filters.roleGroup"),
                        options: roleGroupOptions,
                    },
                ]}
            />
            <div
                className={cn(
                    "overflow-hidden rounded-md border transition-opacity",
                    isFetching && "opacity-60"
                )}
            >
                <Table>
                    <TableHeader>
                        {table.getHeaderGroups().map((headerGroup) => (
                            <TableRow
                                key={headerGroup.id}
                                className="group/row"
                            >
                                {headerGroup.headers.map((header) => {
                                    return (
                                        <TableHead
                                            key={header.id}
                                            colSpan={header.colSpan}
                                            className={cn(
                                                "bg-background group-hover/row:bg-muted group-data-[state=selected]/row:bg-muted",
                                                header.column.columnDef.meta
                                                    ?.className,
                                                header.column.columnDef.meta
                                                    ?.thClassName
                                            )}
                                        >
                                            {header.isPlaceholder
                                                ? null
                                                : flexRender(
                                                      header.column.columnDef
                                                          .header,
                                                      header.getContext()
                                                  )}
                                        </TableHead>
                                    );
                                })}
                            </TableRow>
                        ))}
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell
                                    colSpan={columns.length}
                                    className="h-24 text-center"
                                >
                                    {t("loading")}
                                </TableCell>
                            </TableRow>
                        ) : isError ? (
                            <TableRow>
                                <TableCell
                                    colSpan={columns.length}
                                    className="h-24 text-center text-destructive"
                                >
                                    {error?.response?.data?.message ??
                                        t("errorFallback")}
                                </TableCell>
                            </TableRow>
                        ) : table.getRowModel().rows?.length ? (
                            table.getRowModel().rows.map((row) => (
                                <TableRow
                                    key={row.id}
                                    data-state={
                                        row.getIsSelected() && "selected"
                                    }
                                    className="group/row"
                                >
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell
                                            key={cell.id}
                                            className={cn(
                                                "bg-background group-hover/row:bg-muted group-data-[state=selected]/row:bg-muted",
                                                cell.column.columnDef.meta
                                                    ?.className,
                                                cell.column.columnDef.meta
                                                    ?.tdClassName
                                            )}
                                        >
                                            {flexRender(
                                                cell.column.columnDef.cell,
                                                cell.getContext()
                                            )}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell
                                    colSpan={columns.length}
                                    className="h-24 text-center"
                                >
                                    {t("noResults")}
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
            <DataTablePagination table={table} className="mt-auto" />
            <DataTableBulkActions
                table={table}
                canDeleteUsers={canDeleteUsers}
            />
        </div>
    );
}
