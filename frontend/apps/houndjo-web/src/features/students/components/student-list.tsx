"use client";

import { useGetStudents } from "@api-client";
import { Pencil, Trash2 } from "lucide-react";
import { useTranslations } from "next-intl";
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
import { mapStudentToRow, type StudentRow } from "../data/schema";

const PAGEABLE = { page: 0, size: 100 };

type StudentListProps = {
    search: string;
    canUpdate: boolean;
    canDelete: boolean;
    onEdit: (row: StudentRow) => void;
    onDelete: (row: StudentRow) => void;
};

export function StudentList({
    search,
    canUpdate,
    canDelete,
    onEdit,
    onDelete,
}: StudentListProps) {
    const t = useTranslations("Students.list");
    const { data, isLoading, isError } = useGetStudents({
        search: search.trim() || undefined,
        pageable: PAGEABLE,
    });
    const rows = (data?.items ?? []).map(mapStudentToRow);

    if (isLoading) {
        return <p className="text-sm text-muted-foreground">{t("loading")}</p>;
    }

    if (isError) {
        return <p className="text-sm text-destructive">{t("errorFallback")}</p>;
    }

    if (rows.length === 0) {
        return (
            <p className="text-sm text-muted-foreground">{t("noResults")}</p>
        );
    }

    return (
        <>
            {/* Desktop: table */}
            <div className="hidden overflow-hidden rounded-md border sm:block">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>{t("columns.name")}</TableHead>
                            <TableHead>{t("columns.birthDate")}</TableHead>
                            <TableHead>{t("columns.guardian")}</TableHead>
                            {(canUpdate || canDelete) && (
                                <TableHead className="text-end">
                                    {t("columns.actions")}
                                </TableHead>
                            )}
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {rows.map((row) => (
                            <TableRow key={row.id}>
                                <TableCell className="font-medium">
                                    {row.firstName} {row.lastName}
                                </TableCell>
                                <TableCell className="text-muted-foreground">
                                    {row.birthDate ?? "—"}
                                </TableCell>
                                <TableCell className="text-muted-foreground">
                                    {row.guardianName ?? "—"}
                                    {row.guardianPhone
                                        ? ` (${row.guardianPhone})`
                                        : ""}
                                </TableCell>
                                {(canUpdate || canDelete) && (
                                    <TableCell className="text-end">
                                        <div className="flex justify-end gap-2">
                                            {canUpdate && (
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    aria-label={t("editAction")}
                                                    onClick={() => onEdit(row)}
                                                >
                                                    <Pencil size={16} />
                                                </Button>
                                            )}
                                            {canDelete && (
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    aria-label={t(
                                                        "deleteAction"
                                                    )}
                                                    onClick={() =>
                                                        onDelete(row)
                                                    }
                                                >
                                                    <Trash2
                                                        size={16}
                                                        className="text-destructive"
                                                    />
                                                </Button>
                                            )}
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
                                {row.firstName} {row.lastName}
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-2">
                            {row.birthDate && (
                                <p className="text-sm text-muted-foreground">
                                    {t("columns.birthDate")}: {row.birthDate}
                                </p>
                            )}
                            {row.guardianName && (
                                <p className="text-sm text-muted-foreground">
                                    {t("columns.guardian")}: {row.guardianName}
                                    {row.guardianPhone
                                        ? ` (${row.guardianPhone})`
                                        : ""}
                                </p>
                            )}
                            {(canUpdate || canDelete) && (
                                <div className="flex justify-end gap-2">
                                    {canUpdate && (
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => onEdit(row)}
                                        >
                                            <Pencil
                                                size={16}
                                                className="me-1"
                                            />{" "}
                                            {t("editAction")}
                                        </Button>
                                    )}
                                    {canDelete && (
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => onDelete(row)}
                                        >
                                            <Trash2
                                                size={16}
                                                className="me-1 text-destructive"
                                            />{" "}
                                            {t("deleteAction")}
                                        </Button>
                                    )}
                                </div>
                            )}
                        </CardContent>
                    </Card>
                ))}
            </div>
        </>
    );
}
