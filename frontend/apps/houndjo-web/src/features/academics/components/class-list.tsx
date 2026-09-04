"use client";

import Link from "next/link";
import { useTranslations } from "next-intl";
import { useGetClasses } from "@api-client";
import { Pencil, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { mapClassToRow, type ClassRow } from "../data/schema";

const PAGEABLE = { pageable: { page: 0, size: 100 } };

type ClassListProps = {
    canManage: boolean;
    onEdit: (row: ClassRow) => void;
    onDelete: (row: ClassRow) => void;
};

export function ClassList({ canManage, onEdit, onDelete }: ClassListProps) {
    const t = useTranslations("Classes.list");
    const { data, isLoading, isError } = useGetClasses(PAGEABLE);
    const rows = (data?.items ?? []).map(mapClassToRow);

    if (isLoading) {
        return <p className="text-sm text-muted-foreground">{t("loading")}</p>;
    }

    if (isError) {
        return <p className="text-sm text-destructive">{t("errorFallback")}</p>;
    }

    if (rows.length === 0) {
        return <p className="text-sm text-muted-foreground">{t("noResults")}</p>;
    }

    return (
        <>
            {/* Desktop: table */}
            <div className="hidden overflow-hidden rounded-md border sm:block">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>{t("columns.name")}</TableHead>
                            <TableHead>{t("columns.description")}</TableHead>
                            <TableHead>{t("columns.courseCount")}</TableHead>
                            {canManage && <TableHead className="text-end">{t("columns.actions")}</TableHead>}
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {rows.map((row) => (
                            <TableRow key={row.id}>
                                <TableCell className="font-medium">
                                    <Link href={`/classes/${row.id}`} className="hover:underline">
                                        {row.name}
                                    </Link>
                                </TableCell>
                                <TableCell className="text-muted-foreground">
                                    {row.description ?? "—"}
                                </TableCell>
                                <TableCell>{row.courseCount}</TableCell>
                                {canManage && (
                                    <TableCell className="text-end">
                                        <div className="flex justify-end gap-2">
                                            <Button
                                                variant="ghost"
                                                size="icon"
                                                aria-label={t("editAction")}
                                                onClick={() => onEdit(row)}
                                            >
                                                <Pencil size={16} />
                                            </Button>
                                            <Button
                                                variant="ghost"
                                                size="icon"
                                                aria-label={t("deleteAction")}
                                                onClick={() => onDelete(row)}
                                            >
                                                <Trash2 size={16} className="text-destructive" />
                                            </Button>
                                        </div>
                                    </TableCell>
                                )}
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>

            {/* Mobile: stacked cards */}
            <div className="flex flex-col gap-3 sm:hidden">
                {rows.map((row) => (
                    <Card key={row.id}>
                        <CardHeader>
                            <CardTitle className="text-base">
                                <Link href={`/classes/${row.id}`} className="hover:underline">
                                    {row.name}
                                </Link>
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-2">
                            {row.description && (
                                <p className="text-sm text-muted-foreground">{row.description}</p>
                            )}
                            <p className="text-sm text-muted-foreground">
                                {t("columns.courseCount")}: {row.courseCount}
                            </p>
                            {canManage && (
                                <div className="flex justify-end gap-2">
                                    <Button variant="outline" size="sm" onClick={() => onEdit(row)}>
                                        <Pencil size={16} className="me-1" /> {t("editAction")}
                                    </Button>
                                    <Button variant="outline" size="sm" onClick={() => onDelete(row)}>
                                        <Trash2 size={16} className="me-1 text-destructive" />{" "}
                                        {t("deleteAction")}
                                    </Button>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                ))}
            </div>
        </>
    );
}
