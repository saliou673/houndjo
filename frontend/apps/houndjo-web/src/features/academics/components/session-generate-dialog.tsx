"use client";

import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useGenerateSessions } from "@api-client";
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

type SessionGenerateDialogProps = {
    courseId: number;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

export function SessionGenerateDialog({
    courseId,
    open,
    onOpenChange,
}: SessionGenerateDialogProps) {
    const t = useTranslations("Classes.sessions.generateDialog");
    const queryClient = useQueryClient();
    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");

    const { mutate, isPending } = useGenerateSessions({
        mutation: {
            onSuccess: async (generated) => {
                await queryClient.invalidateQueries({
                    queryKey: [
                        {
                            url: "/api/v1/courses/:courseId/sessions",
                            params: { courseId },
                        },
                    ],
                });
                toast.success(t("successToast", { count: generated.length }));
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const canSubmit = !!fromDate && !!toDate;

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
                <div className="grid grid-cols-2 gap-3">
                    <div className="space-y-1.5">
                        <Label htmlFor="generate-from-date">{t("fromDate")}</Label>
                        <Input
                            id="generate-from-date"
                            type="date"
                            disabled={isPending}
                            value={fromDate}
                            onChange={(event) => setFromDate(event.target.value)}
                        />
                    </div>
                    <div className="space-y-1.5">
                        <Label htmlFor="generate-to-date">{t("toDate")}</Label>
                        <Input
                            id="generate-to-date"
                            type="date"
                            disabled={isPending}
                            value={toDate}
                            onChange={(event) => setToDate(event.target.value)}
                        />
                    </div>
                </div>
                <DialogFooter>
                    <Button
                        type="button"
                        disabled={isPending || !canSubmit}
                        className="w-full sm:w-auto"
                        onClick={() =>
                            mutate({ courseId, data: { fromDate, toDate } })
                        }
                    >
                        {t("submit")}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
