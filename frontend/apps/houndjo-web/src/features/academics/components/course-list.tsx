"use client";

import { useTranslations } from "next-intl";
import { useGetCourses } from "@api-client";
import { Pencil, Trash2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
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
import { mapCourseToRow, type CourseRow } from "../data/schema";

const PAGEABLE = { pageable: { page: 0, size: 100 } };

type CourseListProps = {
    classId: number;
    canManage: boolean;
    onEdit: (row: CourseRow) => void;
    onDelete: (row: CourseRow) => void;
};

function courseSummary(row: CourseRow, t: ReturnType<typeof useTranslations>): string {
    if (row.type === "QURAN") {
        return t("summary.quran", {
            mode: row.quranMode ? t(`typeOptions.quranMode.${row.quranMode}`) : "",
            fromJuz: row.quranScopeFromJuz ?? "?",
            toJuz: row.quranScopeToJuz ?? "?",
        });
    }
    if (row.type === "BOOK") {
        return row.bookTitle ?? "";
    }
    return "";
}

export function CourseList({ classId, canManage, onEdit, onDelete }: CourseListProps) {
    const t = useTranslations("Classes.courseList");
    const { data, isLoading, isError } = useGetCourses(classId, PAGEABLE);
    const rows = (data?.items ?? []).map(mapCourseToRow);

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
                            <TableHead>{t("columns.type")}</TableHead>
                            <TableHead>{t("columns.details")}</TableHead>
                            {canManage && (
                                <TableHead className="text-end">{t("columns.actions")}</TableHead>
                            )}
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {rows.map((row) => (
                            <TableRow key={row.id}>
                                <TableCell className="font-medium">{row.name}</TableCell>
                                <TableCell>
                                    <Badge variant="secondary">{t(`typeOptions.${row.type}`)}</Badge>
                                </TableCell>
                                <TableCell className="text-muted-foreground">
                                    {courseSummary(row, t)}
                                </TableCell>
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
                            <CardTitle className="flex items-center gap-2 text-base">
                                {row.name}
                                <Badge variant="secondary">{t(`typeOptions.${row.type}`)}</Badge>
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-2">
                            <p className="text-sm text-muted-foreground">{courseSummary(row, t)}</p>
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
