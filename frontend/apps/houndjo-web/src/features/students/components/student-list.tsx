"use client";

import { useGetStudents } from "@api-client";
import { CalendarCheck, Pencil, Trash2 } from "lucide-react";
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
    actions: ReadonlySet<"update" | "delete" | "attendance">;
    onEdit: (row: StudentRow) => void;
    onDelete: (row: StudentRow) => void;
    onViewAttendance: (row: StudentRow) => void;
};

export function StudentList({
    search,
    actions,
    onEdit,
    onDelete,
    onViewAttendance,
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
                            {actions.size > 0 && (
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
                                {actions.size > 0 && (
                                    <TableCell className="text-end">
                                        <div className="flex justify-end gap-2">
                                            {actions.has("attendance") && (
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    aria-label={t(
                                                        "viewAttendanceAction"
                                                    )}
                                                    onClick={() =>
                                                        onViewAttendance(row)
                                                    }
                                                >
                                                    <CalendarCheck size={16} />
                                                </Button>
                                            )}
                                            {actions.has("update") && (
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    aria-label={t("editAction")}
                                                    onClick={() => onEdit(row)}
                                                >
                                                    <Pencil size={16} />
                                                </Button>
                                            )}
                                            {actions.has("delete") && (
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
                            {actions.size > 0 && (
                                <div className="flex flex-wrap justify-end gap-2">
                                    {actions.has("attendance") && (
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() =>
                                                onViewAttendance(row)
                                            }
                                        >
                                            <CalendarCheck
                                                size={16}
                                                className="me-1"
                                            />{" "}
                                            {t("viewAttendanceAction")}
                                        </Button>
                                    )}
                                    {actions.has("update") && (
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
                                    {actions.has("delete") && (
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
