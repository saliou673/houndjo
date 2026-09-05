"use client";

import { useState } from "react";
import { useCancelSession, useGetSessions } from "@api-client";
import {
    CalendarCheck,
    ChevronLeft,
    ChevronRight,
    CircleOff,
    ClipboardList,
} from "lucide-react";
import { useTranslations } from "next-intl";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { AttendanceRollCallDialog } from "@/features/attendance/components/attendance-roll-call-dialog";
import { ProgressEntryDialog } from "@/features/progress/components/progress-entry-dialog";

const PAGE_SIZE = 25;

type SessionListProps = {
    classId: number;
    courseId: number;
    canUpdate: boolean;
    canRecordProgress: boolean;
    canRecordAttendance: boolean;
};

export function SessionList({
    classId,
    courseId,
    canUpdate,
    canRecordProgress,
    canRecordAttendance,
}: SessionListProps) {
    const t = useTranslations("Classes.sessions");
    const tDataTable = useTranslations("DataTable");
    const queryClient = useQueryClient();
    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");
    const [page, setPage] = useState(0);
    const [progressSession, setProgressSession] = useState<{
        id: number;
        date: string;
    } | null>(null);
    const [attendanceSession, setAttendanceSession] = useState<{
        id: number;
        date: string;
    } | null>(null);

    const { data, isLoading, isError } = useGetSessions(courseId, {
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
        pageable: { page, size: PAGE_SIZE },
    });
    const rows = data?.items ?? [];
    const totalPages = data?.totalPages ?? 0;

    const { mutate: cancelSession } = useCancelSession({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: [
                        {
                            url: "/api/v1/courses/:courseId/sessions",
                            params: { courseId },
                        },
                    ],
                });
                toast.success(t("cancelSuccessToast"));
            },
            onError: handleServerError,
        },
    });

    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-lg">{t("listTitle")}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
                <div className="flex flex-wrap items-end gap-3">
                    <div className="space-y-1.5">
                        <Label htmlFor="session-from-date">
                            {t("filters.fromDate")}
                        </Label>
                        <Input
                            id="session-from-date"
                            type="date"
                            value={fromDate}
                            onChange={(event) => {
                                setFromDate(event.target.value);
                                setPage(0);
                            }}
                        />
                    </div>
                    <div className="space-y-1.5">
                        <Label htmlFor="session-to-date">
                            {t("filters.toDate")}
                        </Label>
                        <Input
                            id="session-to-date"
                            type="date"
                            value={toDate}
                            onChange={(event) => {
                                setToDate(event.target.value);
                                setPage(0);
                            }}
                        />
                    </div>
                </div>

                {isLoading && (
                    <p className="text-sm text-muted-foreground">{t("loading")}</p>
                )}
                {isError && (
                    <p className="text-sm text-destructive">{t("errorFallback")}</p>
                )}
                {!isLoading && !isError && rows.length === 0 && (
                    <p className="text-sm text-muted-foreground">{t("noResults")}</p>
                )}

                {!isLoading && !isError && rows.length > 0 && (
                    <div className="space-y-3">
                        <div className="overflow-hidden rounded-md border">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>{t("columns.date")}</TableHead>
                                        <TableHead>{t("columns.time")}</TableHead>
                                        <TableHead>{t("columns.teacher")}</TableHead>
                                        <TableHead>{t("columns.status")}</TableHead>
                                        {(canUpdate ||
                                            canRecordProgress ||
                                            canRecordAttendance) && (
                                            <TableHead className="text-end">
                                                {t("columns.actions")}
                                            </TableHead>
                                        )}
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {rows.map((session) => (
                                        <TableRow key={session.id}>
                                            <TableCell className="font-medium">
                                                {session.sessionDate}
                                            </TableCell>
                                            <TableCell className="text-muted-foreground">
                                                {session.startTime && session.endTime
                                                    ? `${session.startTime}–${session.endTime}`
                                                    : "—"}
                                            </TableCell>
                                            <TableCell className="text-muted-foreground">
                                                {session.teacherName ?? "—"}
                                            </TableCell>
                                            <TableCell>
                                                <Badge
                                                    variant={
                                                        session.status === "CANCELLED"
                                                            ? "secondary"
                                                            : "default"
                                                    }
                                                >
                                                    {t(`statusOptions.${session.status}`)}
                                                </Badge>
                                            </TableCell>
                                            {(canUpdate ||
                                                canRecordProgress ||
                                                canRecordAttendance) && (
                                                <TableCell className="text-end">
                                                    {canRecordAttendance &&
                                                        session.status !== "CANCELLED" && (
                                                            <Button
                                                                variant="ghost"
                                                                size="icon"
                                                                aria-label={t(
                                                                    "recordAttendanceAction"
                                                                )}
                                                                onClick={() =>
                                                                    setAttendanceSession({
                                                                        id: session.id ?? 0,
                                                                        date:
                                                                            session.sessionDate ??
                                                                            "",
                                                                    })
                                                                }
                                                            >
                                                                <CalendarCheck size={16} />
                                                            </Button>
                                                        )}
                                                    {canRecordProgress &&
                                                        session.status !== "CANCELLED" && (
                                                            <Button
                                                                variant="ghost"
                                                                size="icon"
                                                                aria-label={t(
                                                                    "recordProgressAction"
                                                                )}
                                                                onClick={() =>
                                                                    setProgressSession({
                                                                        id: session.id ?? 0,
                                                                        date:
                                                                            session.sessionDate ??
                                                                            "",
                                                                    })
                                                                }
                                                            >
                                                                <ClipboardList size={16} />
                                                            </Button>
                                                        )}
                                                    {canUpdate && session.status === "PLANNED" && (
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            aria-label={t("cancelAction")}
                                                            onClick={() =>
                                                                cancelSession({
                                                                    courseId,
                                                                    id: session.id ?? 0,
                                                                })
                                                            }
                                                        >
                                                            <CircleOff
                                                                size={16}
                                                                className="text-destructive"
                                                            />
                                                        </Button>
                                                    )}
                                                </TableCell>
                                            )}
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </div>
                        {totalPages > 1 && (
                            <div className="flex items-center justify-end gap-2">
                                <span className="text-sm text-muted-foreground">
                                    {tDataTable("pageOf", {
                                        page: page + 1,
                                        total: totalPages,
                                    })}
                                </span>
                                <Button
                                    type="button"
                                    variant="outline"
                                    size="icon"
                                    aria-label={tDataTable("goToPreviousPage")}
                                    disabled={page === 0}
                                    onClick={() => setPage((current) => current - 1)}
                                >
                                    <ChevronLeft className="size-4 rtl:rotate-180" />
                                </Button>
                                <Button
                                    type="button"
                                    variant="outline"
                                    size="icon"
                                    aria-label={tDataTable("goToNextPage")}
                                    disabled={page + 1 >= totalPages}
                                    onClick={() => setPage((current) => current + 1)}
                                >
                                    <ChevronRight className="size-4 rtl:rotate-180" />
                                </Button>
                            </div>
                        )}
                    </div>
                )}
            </CardContent>

            {progressSession && canRecordProgress && (
                <ProgressEntryDialog
                    key={`progress-entry-${progressSession.id}`}
                    classId={classId}
                    courseId={courseId}
                    sessionId={progressSession.id}
                    sessionDate={progressSession.date}
                    open={!!progressSession}
                    onOpenChange={(nextOpen) => !nextOpen && setProgressSession(null)}
                />
            )}

            {attendanceSession && canRecordAttendance && (
                <AttendanceRollCallDialog
                    key={`attendance-roll-call-${attendanceSession.id}`}
                    classId={classId}
                    courseId={courseId}
                    sessionId={attendanceSession.id}
                    sessionDate={attendanceSession.date}
                    open={!!attendanceSession}
                    onOpenChange={(nextOpen) => !nextOpen && setAttendanceSession(null)}
                />
            )}
        </Card>
    );
}
