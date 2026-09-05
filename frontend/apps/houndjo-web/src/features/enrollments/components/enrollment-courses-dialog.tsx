"use client";

import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useGetCourses, useUpdateEnrollmentCourses } from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { type EnrollmentRow } from "../data/schema";

const LIST_PAGEABLE = { page: 0, size: 100 };

type EnrollmentCoursesDialogProps = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    currentRow: EnrollmentRow;
};

export function EnrollmentCoursesDialog({
    open,
    onOpenChange,
    currentRow,
}: EnrollmentCoursesDialogProps) {
    const t = useTranslations("Enrollments.coursesDialog");
    const queryClient = useQueryClient();
    const { data: coursesData } = useGetCourses(currentRow.classId, {
        pageable: LIST_PAGEABLE,
    });
    const courses = coursesData?.items ?? [];

    const [selected, setSelected] = useState<Set<number>>(
        () => new Set(currentRow.courseIds)
    );

    const { mutate, isPending } = useUpdateEnrollmentCourses({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: [{ url: "/api/v1/enrollments" }],
                });
                toast.success(t("successToast"));
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const onSubmit = () => {
        const original = new Set(currentRow.courseIds);
        const addCourseIds = [...selected].filter((id) => !original.has(id));
        const removeCourseIds = [...original].filter((id) => !selected.has(id));
        mutate({
            id: currentRow.id,
            data: { addCourseIds, removeCourseIds },
        });
    };

    return (
        <Dialog
            open={open}
            onOpenChange={(nextOpen) => {
                if (!isPending) {
                    onOpenChange(nextOpen);
                }
            }}
        >
            <DialogContent className="flex max-h-[90vh] flex-col overflow-y-auto sm:max-w-lg">
                <DialogHeader className="text-start">
                    <DialogTitle>{t("title")}</DialogTitle>
                    <DialogDescription>
                        {t("description", {
                            name: currentRow.studentName,
                            className: currentRow.className,
                        })}
                    </DialogDescription>
                </DialogHeader>
                <div className="space-y-2 rounded-md border p-3">
                    {courses.length === 0 && (
                        <p className="text-sm text-muted-foreground">
                            {t("noCourses")}
                        </p>
                    )}
                    {courses.map((course) => {
                        const id = course.id ?? 0;
                        const checked = selected.has(id);
                        return (
                            <div key={id} className="flex items-center gap-2">
                                <Checkbox
                                    id={`enrollment-course-${id}`}
                                    checked={checked}
                                    disabled={isPending}
                                    onCheckedChange={(value) => {
                                        setSelected((previous) => {
                                            const next = new Set(previous);
                                            if (value) {
                                                next.add(id);
                                            } else {
                                                next.delete(id);
                                            }
                                            return next;
                                        });
                                    }}
                                />
                                <Label htmlFor={`enrollment-course-${id}`}>
                                    {course.name}
                                </Label>
                            </div>
                        );
                    })}
                </div>
                <DialogFooter>
                    <Button
                        type="button"
                        disabled={isPending}
                        className="w-full sm:w-auto"
                        onClick={onSubmit}
                    >
                        {t("submit")}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
