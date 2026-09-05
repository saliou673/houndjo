"use client";

import { useState } from "react";
import {
    getAttendanceQueryKey,
    useActiveCourseEnrollments,
    useGetAttendance,
    useRecordBulkAttendance,
    type AttendanceStatusEnumKey,
} from "@api-client";
import { useTranslations } from "next-intl";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { type AttendanceEntryState } from "../data/schema";
import { AttendanceStatusToggle } from "./attendance-status-toggle";

type AttendanceRollCallDialogProps = {
    classId: number;
    courseId: number;
    sessionId: number;
    sessionDate: string;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

export function AttendanceRollCallDialog({
    classId,
    courseId,
    sessionId,
    sessionDate,
    open,
    onOpenChange,
}: AttendanceRollCallDialogProps) {
    const t = useTranslations("Attendance.rollCallDialog");
    const queryClient = useQueryClient();
    // Only holds entries the user actively edited in this session; anything else
    // falls back to the previously recorded attendance, then to PRESENT by default.
    const [overrides, setOverrides] = useState<
        Record<number, AttendanceEntryState>
    >({});

    const {
        data: enrollments,
        isLoading: isEnrollmentsLoading,
        isError: isEnrollmentsError,
    } = useActiveCourseEnrollments(classId, courseId, open);
    const students = enrollments?.items ?? [];

    const { data: existingAttendance, isLoading: isExistingLoading } =
        useGetAttendance(sessionId, undefined, { query: { enabled: open } });
    const existingByStudentId: Record<number, AttendanceEntryState> = {};
    for (const attendance of existingAttendance ?? []) {
        if (attendance.studentId != null) {
            existingByStudentId[attendance.studentId] = {
                status: attendance.status ?? "PRESENT",
                reason: attendance.reason ?? "",
            };
        }
    }

    const isLoading = isEnrollmentsLoading || isExistingLoading;

    const getEntry = (studentId: number): AttendanceEntryState =>
        overrides[studentId] ??
        existingByStudentId[studentId] ?? { status: "PRESENT", reason: "" };

    const setStatus = (studentId: number, status: AttendanceStatusEnumKey) => {
        setOverrides((current) => ({
            ...current,
            [studentId]: { ...getEntry(studentId), status },
        }));
    };

    const setReason = (studentId: number, reason: string) => {
        setOverrides((current) => ({
            ...current,
            [studentId]: { ...getEntry(studentId), reason },
        }));
    };

    const { mutate, isPending } = useRecordBulkAttendance({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: getAttendanceQueryKey(sessionId),
                });
                toast.success(t("successToast"));
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const onSubmit = () => {
        mutate({
            sessionId,
            data: {
                entries: students.map((enrollment) => {
                    const studentId = enrollment.studentId ?? 0;
                    const entry = getEntry(studentId);
                    return {
                        studentId,
                        status: entry.status,
                        reason: entry.reason.trim() || undefined,
                    };
                }),
            },
        });
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="flex max-h-[90vh] flex-col overflow-y-auto sm:max-w-2xl">
                <DialogHeader className="text-start">
                    <DialogTitle>{t("title")}</DialogTitle>
                    <DialogDescription>
                        {t("description", { date: sessionDate })}
                    </DialogDescription>
                </DialogHeader>

                {isLoading && (
                    <p className="text-sm text-muted-foreground">{t("loading")}</p>
                )}
                {isEnrollmentsError && (
                    <p className="text-sm text-destructive">{t("errorFallback")}</p>
                )}
                {!isLoading && !isEnrollmentsError && students.length === 0 && (
                    <p className="text-sm text-muted-foreground">{t("noStudents")}</p>
                )}

                {!isLoading && !isEnrollmentsError && students.length > 0 && (
                    <>
                        {/* Desktop: table */}
                        <div className="hidden overflow-hidden rounded-md border sm:block">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>{t("columns.student")}</TableHead>
                                        <TableHead>{t("columns.status")}</TableHead>
                                        <TableHead>{t("columns.reason")}</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {students.map((enrollment) => {
                                        const studentId = enrollment.studentId ?? 0;
                                        const entry = getEntry(studentId);
                                        return (
                                            <TableRow key={studentId}>
                                                <TableCell className="font-medium">
                                                    {enrollment.studentName}
                                                </TableCell>
                                                <TableCell>
                                                    <AttendanceStatusToggle
                                                        value={entry.status}
                                                        onChange={(status) =>
                                                            setStatus(studentId, status)
                                                        }
                                                        disabled={isPending}
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    {entry.status !== "PRESENT" && (
                                                        <Input
                                                            value={entry.reason}
                                                            onChange={(event) =>
                                                                setReason(
                                                                    studentId,
                                                                    event.target.value
                                                                )
                                                            }
                                                            placeholder={t(
                                                                "reasonPlaceholder"
                                                            )}
                                                            disabled={isPending}
                                                        />
                                                    )}
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })}
                                </TableBody>
                            </Table>
                        </div>

                        {/* Mobile: stacked cards */}
                        <div className="flex flex-col gap-3 sm:hidden">
                            {students.map((enrollment) => {
                                const studentId = enrollment.studentId ?? 0;
                                const entry = getEntry(studentId);
                                return (
                                    <Card key={studentId}>
                                        <CardContent className="space-y-2 pt-4">
                                            <p className="font-medium">
                                                {enrollment.studentName}
                                            </p>
                                            <AttendanceStatusToggle
                                                value={entry.status}
                                                onChange={(status) =>
                                                    setStatus(studentId, status)
                                                }
                                                disabled={isPending}
                                            />
                                            {entry.status !== "PRESENT" && (
                                                <Input
                                                    value={entry.reason}
                                                    onChange={(event) =>
                                                        setReason(
                                                            studentId,
                                                            event.target.value
                                                        )
                                                    }
                                                    placeholder={t("reasonPlaceholder")}
                                                    disabled={isPending}
                                                />
                                            )}
                                        </CardContent>
                                    </Card>
                                );
                            })}
                        </div>
                    </>
                )}

                <DialogFooter>
                    <Button
                        type="button"
                        onClick={onSubmit}
                        disabled={isLoading || isPending || students.length === 0}
                        className="w-full sm:w-auto"
                    >
                        {t("submit")}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
