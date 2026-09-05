"use client";
import { useState } from "react";
import { useGetAttendanceHistory } from "@api-client";
import { useTranslations } from "next-intl";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { DateRangeFilter } from "@/components/date-range-filter";
import { hasValidId, attendanceAppearance } from "../data/policy";

export function AttendanceHistorySection({ studentId }: { studentId: number }) {
    const t = useTranslations("Attendance.studentDialog");
    const tStatus = useTranslations("Attendance.statusOptions");
    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");
    const {
        data: history,
        isLoading: isHistoryLoading,
        isError,
    } = useGetAttendanceHistory(studentId, {
        from: fromDate || undefined,
        to: toDate || undefined,
    });
    const entries = (history?.entries ?? []).filter(hasValidId);
    const isHistoryError =
        isError ||
        new Set(entries.map((entry) => entry.id)).size !== entries.length ||
        entries.length !== (history?.entries ?? []).length;
    const absenceRatePercent =
        !isHistoryError && history?.absenceRate != null
            ? Math.round(history.absenceRate * 100)
            : null;
    return (
        <section className="space-y-3">
            <div className="flex items-center justify-between gap-2">
                <h3 className="text-sm font-semibold">{t("historyTitle")}</h3>
                {absenceRatePercent !== null && (
                    <Badge
                        variant={
                            absenceRatePercent > 20
                                ? "destructive"
                                : "secondary"
                        }
                    >
                        {t("absenceRate", { rate: absenceRatePercent })}
                    </Badge>
                )}
            </div>

            <DateRangeFilter
                id="attendance-history"
                fromDate={fromDate}
                toDate={toDate}
                onFromChange={setFromDate}
                onToChange={setToDate}
                fromLabel={t("filters.fromDate")}
                toLabel={t("filters.toDate")}
            />
            {isHistoryLoading && (
                <p className="text-sm text-muted-foreground">{t("loading")}</p>
            )}
            {isHistoryError && (
                <p className="text-sm text-destructive">{t("errorFallback")}</p>
            )}
            {!isHistoryLoading && !isHistoryError && entries.length === 0 && (
                <p className="text-sm text-muted-foreground">
                    {t("noHistory")}
                </p>
            )}
            {!isHistoryLoading && !isHistoryError && entries.length > 0 && (
                <>
                    {/* Desktop: table */}
                    <div className="hidden overflow-hidden rounded-md border sm:block">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>{t("columns.date")}</TableHead>
                                    <TableHead>{t("columns.status")}</TableHead>
                                    <TableHead>{t("columns.reason")}</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {entries.map((entry) => (
                                    <TableRow key={entry.id}>
                                        <TableCell>
                                            {entry.sessionDate ?? "—"}
                                        </TableCell>
                                        <TableCell>
                                            <Badge
                                                variant={
                                                    entry.status
                                                        ? attendanceAppearance[
                                                              entry.status
                                                          ].variant
                                                        : "secondary"
                                                }
                                                className={
                                                    entry.status
                                                        ? attendanceAppearance[
                                                              entry.status
                                                          ].className
                                                        : undefined
                                                }
                                            >
                                                {entry.status
                                                    ? tStatus(entry.status)
                                                    : "—"}
                                            </Badge>
                                        </TableCell>
                                        <TableCell className="text-muted-foreground">
                                            {entry.reason ?? "—"}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>

                    {/* Mobile: stacked cards */}
                    <div className="flex flex-col gap-2 sm:hidden">
                        {entries.map((entry) => (
                            <Card key={entry.id}>
                                <CardContent className="space-y-1 pt-4">
                                    <div className="flex items-center justify-between">
                                        <span className="font-medium">
                                            {entry.sessionDate ?? "—"}
                                        </span>
                                        <Badge
                                            variant={
                                                entry.status
                                                    ? attendanceAppearance[
                                                          entry.status
                                                      ].variant
                                                    : "secondary"
                                            }
                                            className={
                                                entry.status
                                                    ? attendanceAppearance[
                                                          entry.status
                                                      ].className
                                                    : undefined
                                            }
                                        >
                                            {entry.status
                                                ? tStatus(entry.status)
                                                : "—"}
                                        </Badge>
                                    </div>
                                    {entry.reason && (
                                        <p className="text-sm text-muted-foreground">
                                            {entry.reason}
                                        </p>
                                    )}
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                </>
            )}
        </section>
    );
}
