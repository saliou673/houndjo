"use client";

import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useCreateSession } from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

type SessionFormDialogProps = {
    courseId: number;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

export function SessionFormDialog({
    courseId,
    open,
    onOpenChange,
}: SessionFormDialogProps) {
    const t = useTranslations("Classes.sessions.formDialog");
    const queryClient = useQueryClient();
    const [sessionDate, setSessionDate] = useState("");
    const [startTime, setStartTime] = useState("");
    const [endTime, setEndTime] = useState("");

    const { mutate, isPending } = useCreateSession({
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
                toast.success(t("successToast"));
                setSessionDate("");
                setStartTime("");
                setEndTime("");
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    return (
        <Dialog
            open={open}
            onOpenChange={(nextOpen) => {
                if (!isPending) {
                    onOpenChange(nextOpen);
                }
            }}
        >
            <DialogContent className="sm:max-w-md">
                <DialogHeader className="text-start">
                    <DialogTitle>{t("title")}</DialogTitle>
                    <DialogDescription>{t("description")}</DialogDescription>
                </DialogHeader>
                <div className="space-y-3">
                    <div className="space-y-1.5">
                        <Label htmlFor="session-date">{t("sessionDate")}</Label>
                        <Input
                            id="session-date"
                            type="date"
                            disabled={isPending}
                            value={sessionDate}
                            onChange={(event) => setSessionDate(event.target.value)}
                        />
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                        <div className="space-y-1.5">
                            <Label htmlFor="session-start-time">
                                {t("startTime")}
                            </Label>
                            <Input
                                id="session-start-time"
                                type="time"
                                disabled={isPending}
                                value={startTime}
                                onChange={(event) => setStartTime(event.target.value)}
                            />
                        </div>
                        <div className="space-y-1.5">
                            <Label htmlFor="session-end-time">{t("endTime")}</Label>
                            <Input
                                id="session-end-time"
                                type="time"
                                disabled={isPending}
                                value={endTime}
                                onChange={(event) => setEndTime(event.target.value)}
                            />
                        </div>
                    </div>
                </div>
                <DialogFooter>
                    <Button
                        type="button"
                        disabled={isPending || !sessionDate}
                        className="w-full sm:w-auto"
                        onClick={() =>
                            mutate({
                                courseId,
                                data: {
                                    sessionDate,
                                    startTime: startTime || undefined,
                                    endTime: endTime || undefined,
                                },
                            })
                        }
                    >
                        {t("submit")}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
