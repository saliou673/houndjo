"use client";
import { useTranslations } from "next-intl";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { AttendanceHistorySection } from "./attendance-history-section";
import { AttendancePermissionForm } from "./attendance-permission-form";
import { AttendancePermissionList } from "./attendance-permission-list";

type Props = {
    studentId: number;
    studentName: string;
    canReadAttendance: boolean;
    canReadPermission: boolean;
    canCreatePermission: boolean;
    canUpdatePermission: boolean;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};
export function StudentAttendanceDialog({
    studentId,
    studentName,
    canReadAttendance,
    canReadPermission,
    canCreatePermission,
    canUpdatePermission,
    open,
    onOpenChange,
}: Props) {
    const t = useTranslations("Attendance.studentDialog");
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="flex max-h-[90vh] flex-col overflow-y-auto sm:max-w-2xl">
                <DialogHeader className="text-start">
                    <DialogTitle>
                        {t("title", { name: studentName })}
                    </DialogTitle>
                    <DialogDescription>{t("description")}</DialogDescription>
                </DialogHeader>
                {open && (
                    <div className="space-y-6">
                        {canReadAttendance && (
                            <AttendanceHistorySection studentId={studentId} />
                        )}
                        {(canReadPermission || canCreatePermission) && (
                            <section className="space-y-3 border-t pt-4">
                                <h3 className="text-sm font-semibold">
                                    {t("permissions.title")}
                                </h3>
                                {canReadPermission && (
                                    <AttendancePermissionList
                                        studentId={studentId}
                                        canUpdatePermission={
                                            canUpdatePermission
                                        }
                                    />
                                )}
                                {canCreatePermission && (
                                    <AttendancePermissionForm
                                        studentId={studentId}
                                    />
                                )}
                            </section>
                        )}
                    </div>
                )}
            </DialogContent>
        </Dialog>
    );
}
