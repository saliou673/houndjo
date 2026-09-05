"use client";
import { memo, useMemo } from "react";
import { useForm, useController, type Control } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import {
    useActiveCourseEnrollments,
    useGetAttendance,
    useRecordBulkAttendance,
    getAttendanceQueryKey,
} from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { createRollCallSchema, type RollCallValues } from "../data/schema";
import { AttendanceStatusToggle } from "./attendance-status-toggle";

type Props = {
    classId: number;
    courseId: number;
    sessionId: number;
    sessionDate: string;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};
const RollCallRow = memo(function RollCallRow({
    index,
    name,
    control,
    disabled,
}: {
    index: number;
    name?: string;
    control: Control<RollCallValues>;
    disabled: boolean;
}) {
    const t = useTranslations("Attendance.rollCallDialog");
    const status = useController({ control, name: `entries.${index}.status` });
    const reason = useController({ control, name: `entries.${index}.reason` });
    return (
        <div className="grid gap-3 rounded-md border p-3 sm:grid-cols-[1fr_2fr_1fr] sm:items-center">
            <p className="font-medium">{name}</p>
            <AttendanceStatusToggle
                value={status.field.value}
                onChange={status.field.onChange}
                disabled={disabled}
            />
            <div>
                {status.field.value !== "PRESENT" && (
                    <Input
                        {...reason.field}
                        placeholder={t("reasonPlaceholder")}
                        aria-label={`${name} — ${t("columns.reason")}`}
                        disabled={disabled}
                        aria-invalid={!!reason.fieldState.error}
                    />
                )}
                {reason.fieldState.error && (
                    <p role="alert" className="text-sm text-destructive">
                        {reason.fieldState.error.message}
                    </p>
                )}
            </div>
        </div>
    );
});
export function AttendanceRollCallDialog({
    classId,
    courseId,
    sessionId,
    sessionDate,
    open,
    onOpenChange,
}: Props) {
    const t = useTranslations("Attendance.rollCallDialog");
    const queryClient = useQueryClient();
    const roster = useActiveCourseEnrollments(classId, courseId, open);
    const existing = useGetAttendance(sessionId, undefined, {
        query: { enabled: open },
    });
    const students = roster.data?.items;
    const invalidRoster =
        students?.some(
            (student) =>
                !Number.isSafeInteger(student.studentId) ||
                Number(student.studentId) <= 0
        ) ||
        new Set(students?.map((student) => student.studentId)).size !==
            (students?.length ?? 0);
    const values = useMemo(() => {
        const byStudent = new Map(
            existing.data?.map((entry) => [entry.studentId, entry])
        );
        return {
            entries: Object.fromEntries(
                (students ?? []).map((student) => [
                    String(student.studentId),
                    {
                        studentId: student.studentId!,
                        status:
                            byStudent.get(student.studentId)?.status ??
                            "PRESENT",
                        reason: byStudent.get(student.studentId)?.reason ?? "",
                    },
                ])
            ),
        };
    }, [students, existing.data]);
    const schema = useMemo(() => createRollCallSchema(t), [t]);
    const form = useForm<RollCallValues>({
        resolver: zodResolver(schema),
        values,
        resetOptions: { keepDirtyValues: true },
    });
    const { mutateAsync, isPending } = useRecordBulkAttendance();
    const isLoading = roster.isLoading || existing.isLoading;
    const isError = roster.isError || existing.isError || invalidRoster;
    const canSubmit =
        roster.isSuccess &&
        existing.isSuccess &&
        !isError &&
        !!students?.length &&
        !isPending;
    const onSubmit = form.handleSubmit(async (data) => {
        if (!canSubmit) return;
        try {
            await mutateAsync({
                sessionId,
                data: {
                    entries: Object.values(data.entries).map((entry) => ({
                        ...entry,
                        reason: entry.reason.trim() || undefined,
                    })),
                },
            });
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: getAttendanceQueryKey(sessionId),
                }),
                queryClient.invalidateQueries({
                    queryKey: [
                        { url: "/api/v1/students/:studentId/attendance" },
                    ],
                }),
            ]);
            toast.success(t("successToast"));
            onOpenChange(false);
        } catch (error) {
            handleServerError(error);
        }
    });
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
                    <p className="text-sm text-muted-foreground">
                        {t("loading")}
                    </p>
                )}
                {isError && (
                    <p role="alert" className="text-sm text-destructive">
                        {t("errorFallback")}
                    </p>
                )}
                {!isLoading && !isError && !students?.length && (
                    <p className="text-sm text-muted-foreground">
                        {t("noStudents")}
                    </p>
                )}
                <form onSubmit={onSubmit} className="space-y-3">
                    {!isLoading &&
                        !isError &&
                        students?.map((student) => (
                            <RollCallRow
                                key={student.studentId}
                                index={student.studentId!}
                                name={student.studentName}
                                control={form.control}
                                disabled={isPending}
                            />
                        ))}
                    <Button
                        type="submit"
                        disabled={!canSubmit}
                        className="w-full sm:w-auto"
                    >
                        {t("submit")}
                    </Button>
                </form>
            </DialogContent>
        </Dialog>
    );
}
