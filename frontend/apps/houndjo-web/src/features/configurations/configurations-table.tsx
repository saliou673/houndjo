"use client";

import { useEffect, useMemo, useState } from "react";
import {
    type ColumnFiltersState,
    type VisibilityState,
    flexRender,
    getCoreRowModel,
    useReactTable,
} from "@tanstack/react-table";
import {
    type AppConfiguration,
    type AppConfigurationCategoryFilterEqualsEnumKey,
    type AppConfigurationCategoryFilterInEnumKey,
    type GetAppConfigurationsAsAdminQueryParams,
    useGetAppConfigurationsAsAdmin,
    useGetCategoriesAsAdmin,
} from "@api-client";
import { cn } from "@/lib/utils";
import { type NavigateFn, useTableUrlState } from "@/hooks/use-table-url-state";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { DataTablePagination } from "@/components/data-table";
import { buildConfigurationsColumns } from "./configurations-columns";
import { ConfigurationsToolbar } from "./configurations-toolbar";

type ConfigurationsTableProps = {
    search: Record<string, unknown>;
    navigate: NavigateFn;
    onEdit: (configuration: AppConfiguration) => void;
    onDelete: (configuration: AppConfiguration) => void;
    onTotalItemsChange?: (totalItems: number) => void;
};

function toStringValue(value: unknown): string {
    return typeof value === "string" ? value : "";
}

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

function toBooleanArray(value: unknown): boolean[] {
    return toStringArray(value)
        .map((item) => {
            if (item === "true") {
                return true;
            }

            if (item === "false") {
                return false;
            }

            return null;
        })
        .filter((item): item is boolean => item !== null);
}

function getArrayFilterValue(
    columnFilters: ColumnFiltersState,
    columnId: string
): string[] {
    const filter = columnFilters.find((item) => item.id === columnId);

    if (!filter) {
        return [];
    }

    if (Array.isArray(filter.value)) {
        return filter.value.filter(
            (item): item is string =>
                typeof item === "string" && item.length > 0
        );
    }

    if (typeof filter.value === "string" && filter.value.length > 0) {
        return [filter.value];
    }

    return [];
}

function buildQueryParams(
    code: string,
    columnFilters: ColumnFiltersState,
    page: number,
    size: number
): GetAppConfigurationsAsAdminQueryParams {
    const trimmedCode = code.trim();
    const categories = getArrayFilterValue(columnFilters, "category");
    const active = toBooleanArray(getArrayFilterValue(columnFilters, "active"));

    return {
        filter: {
            code: trimmedCode ? { contains: trimmedCode } : undefined,
            category:
                categories.length === 1
                    ? {
                          equals: categories[0] as AppConfigurationCategoryFilterEqualsEnumKey,
                      }
                    : categories.length > 1
                      ? {
                            in: categories as AppConfigurationCategoryFilterInEnumKey[],
                        }
                      : undefined,
            active:
                active.length === 1
                    ? { equals: active[0] }
                    : active.length > 1
                      ? { in: active }
                      : undefined,
        },
        pageable: { page, size },
    };
}

export function ConfigurationsTable({
    search,
    navigate,
    onEdit,
    onDelete,
    onTotalItemsChange,
}: ConfigurationsTableProps) {
    const [columnVisibility, setColumnVisibility] = useState<VisibilityState>(
        {}
    );
    const [codeFilter, setCodeFilter] = useState(() =>
        toStringValue(search.code)
    );
    const [debouncedCodeFilter, setDebouncedCodeFilter] = useState(() =>
        toStringValue(search.code)
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
            { columnId: "category", searchKey: "category", type: "array" },
            {
                columnId: "active",
                searchKey: "active",
                type: "array",
                serialize: (value) => value,
                deserialize: (value) =>
                    Array.isArray(value)
                        ? value.map(String)
                        : value !== undefined
                          ? [String(value)]
                          : [],
            },
        ],
    });

    useEffect(() => {
        setCodeFilter(toStringValue(search.code));
    }, [search.code]);

    useEffect(() => {
        const timeout = window.setTimeout(() => {
            setDebouncedCodeFilter(codeFilter);
        }, 300);

        return () => window.clearTimeout(timeout);
    }, [codeFilter]);

    useEffect(() => {
        const trimmedValue = codeFilter.trim();
        const currentValue = toStringValue(search.code).trim();

        if (trimmedValue === currentValue) {
            return;
        }

        const timeout = window.setTimeout(() => {
            navigate({
                search: (prev) => ({
                    ...prev,
                    page: undefined,
                    code: trimmedValue || undefined,
                }),
                replace: true,
            });
        }, 300);

        return () => window.clearTimeout(timeout);
    }, [codeFilter, navigate, search.code]);

    const queryParams = useMemo(
        () =>
            buildQueryParams(
                debouncedCodeFilter,
                columnFilters,
                pagination.pageIndex,
                pagination.pageSize
            ),
        [
            columnFilters,
            debouncedCodeFilter,
            pagination.pageIndex,
            pagination.pageSize,
        ]
    );

    const { data: categoriesData } = useGetCategoriesAsAdmin();
    const categoryOptions = (categoriesData ?? []).map(
        ({ value, description }) => ({
            label: description ?? value ?? "",
            value: value ?? "",
        })
    );

    const { data, isLoading, isError } =
        useGetAppConfigurationsAsAdmin(queryParams);

    const items = data?.items ?? [];
    const totalPages = data?.totalPages ?? 0;
    const totalItems = data?.totalItems ?? 0;
    const columns = buildConfigurationsColumns({ onEdit, onDelete });

    // eslint-disable-next-line react-hooks/incompatible-library
    const table = useReactTable({
        data: items,
        columns,
        pageCount: totalPages,
        state: {
            pagination,
            columnFilters,
            columnVisibility,
        },
        manualPagination: true,
        manualFiltering: true,
        onPaginationChange,
        onColumnFiltersChange,
        onColumnVisibilityChange: setColumnVisibility,
        getCoreRowModel: getCoreRowModel(),
    });

    useEffect(() => {
        ensurePageInRange(totalPages, { resetTo: "last" });
    }, [ensurePageInRange, totalPages]);

    useEffect(() => {
        onTotalItemsChange?.(totalItems);
    }, [onTotalItemsChange, totalItems]);

    return (
        <div
            className={cn(
                'max-sm:has-[div[role="toolbar"]]:mb-16',
                "flex flex-1 flex-col gap-4"
            )}
        >
            <ConfigurationsToolbar
                table={table}
                codeFilter={codeFilter}
                onCodeFilterChange={setCodeFilter}
                categoryOptions={categoryOptions}
                onReset={() => {
                    setCodeFilter("");
                    setDebouncedCodeFilter("");
                    table.resetColumnFilters();
                    navigate({
                        search: (prev) => ({
                            ...prev,
                            page: undefined,
                            code: undefined,
                            category: undefined,
                            active: undefined,
                        }),
                    });
                }}
            />
            <div className="overflow-hidden rounded-md border">
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
                                    Loading configurations...
                                </TableCell>
                            </TableRow>
                        ) : isError ? (
                            <TableRow>
                                <TableCell
                                    colSpan={columns.length}
                                    className="h-24 text-center text-destructive"
                                >
                                    Failed to load configurations.
                                </TableCell>
                            </TableRow>
                        ) : table.getRowModel().rows.length ? (
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
                                    No configurations found.
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
