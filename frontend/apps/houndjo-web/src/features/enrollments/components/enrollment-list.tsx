"use client";

import { useGetEnrollments } from "@api-client";
import { BookOpen, CircleOff } from "lucide-react";
import { useTranslations } from "next-intl";
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
import { mapEnrollmentToRow, type EnrollmentRow } from "../data/schema";
import { type EnrollmentFilters } from "./enrollment-filters";

const PAGEABLE = { page: 0, size: 100 };

type EnrollmentListProps = {
    filters: EnrollmentFilters;
    canManageCourses: boolean;
    canEnd: boolean;
    onManageCourses: (row: EnrollmentRow) => void;
    onEnd: (row: EnrollmentRow) => void;
};

export function EnrollmentList({
    filters,
    canManageCourses,
    canEnd,
    onManageCourses,
    onEnd,
}: EnrollmentListProps) {
    const t = useTranslations("Enrollments.list");
    const { data, isLoading, isError } = useGetEnrollments({
        classId: filters.classId,
        studentId: filters.studentId,
        status: filters.status,
        pageable: PAGEABLE,
    });
    const rows = (data?.items ?? []).map(mapEnrollmentToRow);

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
                            <TableHead>{t("columns.student")}</TableHead>
                            <TableHead>{t("columns.class")}</TableHead>
                            <TableHead>{t("columns.courses")}</TableHead>
                            <TableHead>{t("columns.status")}</TableHead>
                            {(canManageCourses || canEnd) && (
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
                                    {row.studentName}
                                </TableCell>
                                <TableCell>{row.className}</TableCell>
                                <TableCell className="text-muted-foreground">
                                    {row.courseIds.length}
                                </TableCell>
                                <TableCell>
                                    <Badge
                                        variant={
                                            row.status === "ACTIVE"
                                                ? "default"
                                                : "secondary"
                                        }
                                    >
                                        {t(`statusOptions.${row.status}`)}
                                    </Badge>
                                </TableCell>
                                {(canManageCourses || canEnd) && (
                                    <TableCell className="text-end">
                                        <div className="flex justify-end gap-2">
                                            {canManageCourses && (
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    aria-label={t("manageCoursesAction")}
                                                    onClick={() => onManageCourses(row)}
                                                >
                                                    <BookOpen size={16} />
                                                </Button>
                                            )}
                                            {canEnd &&
                                                row.status === "ACTIVE" && (
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        aria-label={t("endAction")}
                                                        onClick={() => onEnd(row)}
                                                    >
                                                        <CircleOff
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
                            <CardTitle className="flex items-center gap-2 text-base">
                                {row.studentName}
                                <Badge
                                    variant={
                                        row.status === "ACTIVE"
                                            ? "default"
                                            : "secondary"
                                    }
                                >
                                    {t(`statusOptions.${row.status}`)}
                                </Badge>
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-2">
                            <p className="text-sm text-muted-foreground">
                                {row.className} · {t("columns.courses")}:{" "}
                                {row.courseIds.length}
                            </p>
                            {(canManageCourses || canEnd) && (
                                <div className="flex justify-end gap-2">
                                    {canManageCourses && (
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => onManageCourses(row)}
                                        >
                                            <BookOpen size={16} className="me-1" />{" "}
                                            {t("manageCoursesAction")}
                                        </Button>
                                    )}
                                    {canEnd && row.status === "ACTIVE" && (
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => onEnd(row)}
                                        >
                                            <CircleOff
                                                size={16}
                                                className="me-1 text-destructive"
                                            />{" "}
                                            {t("endAction")}
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
