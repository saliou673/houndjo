import { useEffect, useMemo, useState } from "react";
import {
    type VisibilityState,
    flexRender,
    getCoreRowModel,
    useReactTable,
} from "@tanstack/react-table";
import { useTranslations } from "next-intl";
import { useGetRoleGroupsAsAdmin } from "@api-client";
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
import { mapRoleGroupToRow } from "../data/schema";
import { buildRoleGroupsColumns } from "./role-groups-columns";

type RoleGroupsTableProps = {
    canManageRoleGroups: boolean;
};

export function RoleGroupsTable({ canManageRoleGroups }: RoleGroupsTableProps) {
    const t = useTranslations("RoleGroups.table");
    const search = useNextSearchObject();
    const navigate = useNextNavigateSearch();
    const [columnVisibility, setColumnVisibility] = useState<VisibilityState>(
        {}
    );
    const [nameSearchValue, setNameSearchValue] = useState("");
    const [debouncedName, setDebouncedName] = useState("");

    useEffect(() => {
        const timeout = window.setTimeout(() => {
            setDebouncedName(nameSearchValue.trim().toLowerCase());
        }, 300);

        return () => window.clearTimeout(timeout);
    }, [nameSearchValue]);

    const { pagination, onPaginationChange, ensurePageInRange } =
        useTableUrlState({
            search,
            navigate,
            pagination: { defaultPage: 1, defaultPageSize: 10 },
            globalFilter: { enabled: false },
            columnFilters: [],
        });

    const queryParams = useMemo(
        () => ({
            pageable: {
                page: pagination.pageIndex,
                size: pagination.pageSize,
            },
        }),
        [pagination.pageIndex, pagination.pageSize]
    );

    const { data, isLoading, isFetching, isError, error } =
        useGetRoleGroupsAsAdmin(queryParams, undefined, {
            query: {
                placeholderData: (previousData) => previousData,
            },
        });

    const allRows = useMemo(
        () => (data?.items ?? []).map(mapRoleGroupToRow),
        [data?.items]
    );

    const rows = useMemo(() => {
        if (!debouncedName) return allRows;
        return allRows.filter((row) =>
            row.name.toLowerCase().includes(debouncedName)
        );
    }, [allRows, debouncedName]);

    const totalPages = data?.totalPages ?? 0;
    const columns = useMemo(
        () =>
            buildRoleGroupsColumns({
                canManageRoleGroups,
                labels: {
                    name: t("columns.name"),
                    description: t("columns.description"),
                    permissions: t("columns.permissions"),
                },
            }),
        [canManageRoleGroups, t]
    );

    // eslint-disable-next-line react-hooks/incompatible-library
    const table = useReactTable({
        data: rows,
        columns,
        pageCount: totalPages,
        state: {
            pagination,
            columnFilters: [],
            columnVisibility,
        },
        manualPagination: true,
        manualFiltering: true,
        getRowId: (row) => String(row.id),
        onPaginationChange,
        onColumnFiltersChange: () => {},
        onColumnVisibilityChange: setColumnVisibility,
        getCoreRowModel: getCoreRowModel(),
    });

    useEffect(() => {
        ensurePageInRange(totalPages);
    }, [ensurePageInRange, totalPages]);

    return (
        <div className="flex flex-1 flex-col gap-4">
            <DataTableToolbar
                table={table}
                searchPlaceholder={t("searchPlaceholder")}
                searchKey="name"
                searchValue={nameSearchValue}
                onSearchChange={setNameSearchValue}
                filters={[]}
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
                                {headerGroup.headers.map((header) => (
                                    <TableHead
                                        key={header.id}
                                        colSpan={header.colSpan}
                                        className={cn(
                                            "bg-background group-hover/row:bg-muted",
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
                                ))}
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
                                <TableRow key={row.id} className="group/row">
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell
                                            key={cell.id}
                                            className={cn(
                                                "bg-background group-hover/row:bg-muted",
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
        </div>
    );
}
