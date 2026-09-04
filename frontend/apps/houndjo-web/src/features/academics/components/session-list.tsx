"use client";

import { useState } from "react";
import { useCancelSession, useGetSessions } from "@api-client";
import { CircleOff } from "lucide-react";
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

const PAGEABLE = { page: 0, size: 100 };

type SessionListProps = {
    courseId: number;
    canUpdate: boolean;
};

export function SessionList({ courseId, canUpdate }: SessionListProps) {
    const t = useTranslations("Classes.sessions");
    const queryClient = useQueryClient();
    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");

    const { data, isLoading, isError } = useGetSessions(courseId, {
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
        pageable: PAGEABLE,
    });
    const rows = data?.items ?? [];

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
                            onChange={(event) => setFromDate(event.target.value)}
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
                            onChange={(event) => setToDate(event.target.value)}
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
                    <div className="overflow-hidden rounded-md border">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>{t("columns.date")}</TableHead>
                                    <TableHead>{t("columns.time")}</TableHead>
                                    <TableHead>{t("columns.teacher")}</TableHead>
                                    <TableHead>{t("columns.status")}</TableHead>
                                    {canUpdate && (
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
                                        {canUpdate && (
                                            <TableCell className="text-end">
                                                {session.status === "PLANNED" && (
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
                )}
            </CardContent>
        </Card>
    );
}
