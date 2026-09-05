"use client";

import { useMemo, useState } from "react";
import {
    getAttendancePermissionsByStudentQueryKey,
    useCreateAttendancePermission,
    useGetAttendanceHistory,
    useGetAttendancePermissionsByStudent,
    useUpdateAttendancePermissionStatus,
} from "@api-client";
import { zodResolver } from "@hookform/resolvers/zod";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
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
import { Textarea } from "@/components/ui/textarea";
import {
    createAttendancePermissionSchema,
    type AttendancePermissionForm,
} from "../data/schema";

type StudentAttendanceDialogProps = {
    studentId: number;
    studentName: string;
    canCreatePermission: boolean;
    canUpdatePermission: boolean;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

const DEFAULT_VALUES: AttendancePermissionForm = {
    fromDate: "",
    toDate: "",
    reason: "",
};

export function StudentAttendanceDialog({
    studentId,
    studentName,
    canCreatePermission,
    canUpdatePermission,
    open,
    onOpenChange,
}: StudentAttendanceDialogProps) {
    const canManagePermissions = canCreatePermission || canUpdatePermission;
    const t = useTranslations("Attendance.studentDialog");
    const tValidation = useTranslations("Attendance.studentDialog.validation");
    const tStatus = useTranslations("Attendance.statusOptions");
    const tPermissionStatus = useTranslations("Attendance.permissionStatusOptions");
    const queryClient = useQueryClient();

    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");

    const {
        data: history,
        isLoading: isHistoryLoading,
        isError: isHistoryError,
    } = useGetAttendanceHistory(
        studentId,
        { from: fromDate || undefined, to: toDate || undefined },
        undefined,
        { query: { enabled: open } }
    );

    const { data: permissions, isLoading: isPermissionsLoading } =
        useGetAttendancePermissionsByStudent({ studentId }, undefined, {
            query: { enabled: open && canManagePermissions },
        });

    const formSchema = useMemo(
        () => createAttendancePermissionSchema(tValidation),
        [tValidation]
    );
    const form = useForm<AttendancePermissionForm>({
        resolver: zodResolver(formSchema),
        defaultValues: DEFAULT_VALUES,
    });

    const invalidatePermissions = () =>
        queryClient.invalidateQueries({
            queryKey: getAttendancePermissionsByStudentQueryKey({ studentId }),
        });

    const { mutate: createPermission, isPending: isCreating } =
        useCreateAttendancePermission({
            mutation: {
                onSuccess: async () => {
                    await invalidatePermissions();
                    toast.success(t("permissions.createSuccessToast"));
                    form.reset(DEFAULT_VALUES);
                },
                onError: handleServerError,
            },
        });

    const { mutate: updateStatus, isPending: isUpdatingStatus } =
        useUpdateAttendancePermissionStatus({
            mutation: {
                onSuccess: async () => {
                    await invalidatePermissions();
                    toast.success(t("permissions.statusUpdatedToast"));
                },
                onError: handleServerError,
            },
        });

    const onSubmitPermission = (values: AttendancePermissionForm) => {
        createPermission({
            data: {
                studentId,
                fromDate: values.fromDate,
                toDate: values.toDate,
                reason: values.reason?.trim() || undefined,
            },
        });
    };

    const entries = history?.entries ?? [];
    const absenceRatePercent =
        history?.absenceRate != null
            ? Math.round(history.absenceRate * 100)
            : null;

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="flex max-h-[90vh] flex-col overflow-y-auto sm:max-w-2xl">
                <DialogHeader className="text-start">
                    <DialogTitle>{t("title", { name: studentName })}</DialogTitle>
                    <DialogDescription>{t("description")}</DialogDescription>
                </DialogHeader>

                <div className="space-y-6">
                    <section className="space-y-3">
                        <div className="flex items-center justify-between gap-2">
                            <h3 className="text-sm font-semibold">
                                {t("historyTitle")}
                            </h3>
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

                        <div className="flex flex-wrap items-end gap-3">
                            <div className="space-y-1.5">
                                <Label htmlFor="attendance-history-from-date">
                                    {t("filters.fromDate")}
                                </Label>
                                <Input
                                    id="attendance-history-from-date"
                                    type="date"
                                    value={fromDate}
                                    onChange={(event) => setFromDate(event.target.value)}
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="attendance-history-to-date">
                                    {t("filters.toDate")}
                                </Label>
                                <Input
                                    id="attendance-history-to-date"
                                    type="date"
                                    value={toDate}
                                    onChange={(event) => setToDate(event.target.value)}
                                />
                            </div>
                        </div>

                        {isHistoryLoading && (
                            <p className="text-sm text-muted-foreground">
                                {t("loading")}
                            </p>
                        )}
                        {isHistoryError && (
                            <p className="text-sm text-destructive">
                                {t("errorFallback")}
                            </p>
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
                                                                entry.status === "PRESENT"
                                                                    ? "default"
                                                                    : "secondary"
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
                                                            entry.status === "PRESENT"
                                                                ? "default"
                                                                : "secondary"
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

                    {canManagePermissions && (
                        <section className="space-y-3 border-t pt-4">
                            <h3 className="text-sm font-semibold">
                                {t("permissions.title")}
                            </h3>

                            {isPermissionsLoading && (
                                <p className="text-sm text-muted-foreground">
                                    {t("loading")}
                                </p>
                            )}
                            {!isPermissionsLoading &&
                                (permissions ?? []).length === 0 && (
                                    <p className="text-sm text-muted-foreground">
                                        {t("permissions.noResults")}
                                    </p>
                                )}
                            {!isPermissionsLoading &&
                                (permissions ?? []).length > 0 && (
                                    <div className="flex flex-col gap-2">
                                        {(permissions ?? []).map((permission) => (
                                            <Card key={permission.id}>
                                                <CardContent className="flex flex-wrap items-center justify-between gap-2 pt-4">
                                                    <div>
                                                        <p className="text-sm font-medium">
                                                            {permission.fromDate} —{" "}
                                                            {permission.toDate}
                                                        </p>
                                                        {permission.reason && (
                                                            <p className="text-sm text-muted-foreground">
                                                                {permission.reason}
                                                            </p>
                                                        )}
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        <Badge
                                                            variant={
                                                                permission.status ===
                                                                "APPROVED"
                                                                    ? "default"
                                                                    : permission.status ===
                                                                        "REJECTED"
                                                                      ? "destructive"
                                                                      : "secondary"
                                                            }
                                                        >
                                                            {permission.status
                                                                ? tPermissionStatus(
                                                                      permission.status
                                                                  )
                                                                : "—"}
                                                        </Badge>
                                                        {permission.status === "PENDING" &&
                                                            canUpdatePermission && (
                                                            <>
                                                                <Button
                                                                    type="button"
                                                                    size="sm"
                                                                    variant="outline"
                                                                    disabled={
                                                                        isUpdatingStatus
                                                                    }
                                                                    onClick={() =>
                                                                        updateStatus({
                                                                            id:
                                                                                permission.id ??
                                                                                0,
                                                                            data: {
                                                                                status:
                                                                                    "APPROVED",
                                                                            },
                                                                        })
                                                                    }
                                                                >
                                                                    {t(
                                                                        "permissions.approveAction"
                                                                    )}
                                                                </Button>
                                                                <Button
                                                                    type="button"
                                                                    size="sm"
                                                                    variant="outline"
                                                                    disabled={
                                                                        isUpdatingStatus
                                                                    }
                                                                    onClick={() =>
                                                                        updateStatus({
                                                                            id:
                                                                                permission.id ??
                                                                                0,
                                                                            data: {
                                                                                status:
                                                                                    "REJECTED",
                                                                            },
                                                                        })
                                                                    }
                                                                >
                                                                    {t(
                                                                        "permissions.rejectAction"
                                                                    )}
                                                                </Button>
                                                            </>
                                                        )}
                                                    </div>
                                                </CardContent>
                                            </Card>
                                        ))}
                                    </div>
                                )}

                            {canCreatePermission && (
                            <Form {...form}>
                                <form
                                    onSubmit={form.handleSubmit(onSubmitPermission)}
                                    className="space-y-3"
                                >
                                    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                                        <FormField
                                            control={form.control}
                                            name="fromDate"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormLabel>
                                                        {t("permissions.fields.fromDate")}
                                                    </FormLabel>
                                                    <FormControl>
                                                        <Input
                                                            type="date"
                                                            disabled={isCreating}
                                                            {...field}
                                                        />
                                                    </FormControl>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                        <FormField
                                            control={form.control}
                                            name="toDate"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormLabel>
                                                        {t("permissions.fields.toDate")}
                                                    </FormLabel>
                                                    <FormControl>
                                                        <Input
                                                            type="date"
                                                            disabled={isCreating}
                                                            {...field}
                                                        />
                                                    </FormControl>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                    </div>
                                    <FormField
                                        control={form.control}
                                        name="reason"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>
                                                    {t("permissions.fields.reason")}
                                                </FormLabel>
                                                <FormControl>
                                                    <Textarea disabled={isCreating} {...field} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                    <Button
                                        type="submit"
                                        disabled={isCreating}
                                        className="w-full sm:w-auto"
                                    >
                                        {t("permissions.requestAction")}
                                    </Button>
                                </form>
                            </Form>
                            )}
                        </section>
                    )}
                </div>
            </DialogContent>
        </Dialog>
    );
}
