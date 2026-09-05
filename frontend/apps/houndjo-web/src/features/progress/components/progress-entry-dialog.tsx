"use client";

import { useState } from "react";
import { useGetCourseById, useActiveCourseEnrollments } from "@api-client";
import { useTranslations } from "next-intl";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ChapterFlowForm } from "./chapter-flow-form";
import { LessonFlowForm } from "./lesson-flow-form";
import { ProgressStateSummary } from "./progress-state-summary";
import { QuranFlowForm } from "./quran-flow-form";

type ProgressEntryDialogProps = {
    classId: number;
    courseId: number;
    sessionId: number;
    sessionDate: string;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

export function ProgressEntryDialog({
    classId,
    courseId,
    sessionId,
    sessionDate,
    open,
    onOpenChange,
}: ProgressEntryDialogProps) {
    const t = useTranslations("Progress.entryDialog");
    const [studentId, setStudentId] = useState<number | undefined>(undefined);

    const { data: course, isLoading: isCourseLoading, isError: isCourseError } =
        useGetCourseById(classId, courseId, undefined, { query: { enabled: open } });
    const { data: enrollments, isLoading: isEnrollmentsLoading, isError: isEnrollmentsError } =
        useActiveCourseEnrollments(classId, courseId, open);
    const students = enrollments?.items ?? [];

    const isLoading = isCourseLoading || isEnrollmentsLoading;

    return (
        <Dialog
            open={open}
            onOpenChange={(nextOpen) => {
                if (!nextOpen) {
                    setStudentId(undefined);
                }
                onOpenChange(nextOpen);
            }}
        >
            <DialogContent className="flex max-h-[90vh] flex-col overflow-y-auto sm:max-w-lg">
                <DialogHeader className="text-start">
                    <DialogTitle>{t("title")}</DialogTitle>
                    <DialogDescription>
                        {t("description", { date: sessionDate })}
                    </DialogDescription>
                </DialogHeader>

                {isLoading && (
                    <p className="text-sm text-muted-foreground">{t("loading")}</p>
                )}
                {(isCourseError || isEnrollmentsError) && (
                    <p className="text-sm text-destructive">{t("errorFallback")}</p>
                )}

                {!isLoading && !isCourseError && !isEnrollmentsError && course && (
                    <div className="space-y-4">
                        <div className="space-y-1.5">
                            <Label htmlFor="progress-student">{t("studentLabel")}</Label>
                            {students.length === 0 ? (
                                <p className="text-sm text-muted-foreground">
                                    {t("noStudents")}
                                </p>
                            ) : (
                                <Select
                                    value={studentId ? String(studentId) : ""}
                                    onValueChange={(value) => setStudentId(Number(value))}
                                >
                                    <SelectTrigger id="progress-student" className="w-full">
                                        <SelectValue placeholder={t("studentPlaceholder")} />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {students.map((enrollment) => (
                                            <SelectItem
                                                key={enrollment.studentId}
                                                value={String(enrollment.studentId)}
                                            >
                                                {enrollment.studentName}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            )}
                        </div>

                        {studentId && (
                            <>
                                {course.type === "QURAN" && (
                                    <ProgressStateSummary studentId={studentId} courseId={courseId} />
                                )}

                                {course.type === "QURAN" && (
                                    <Tabs defaultValue="SABAK">
                                        <TabsList className="w-full">
                                            <TabsTrigger value="SABAK">
                                                {t("tabs.SABAK")}
                                            </TabsTrigger>
                                            <TabsTrigger value="SABQI">
                                                {t("tabs.SABQI")}
                                            </TabsTrigger>
                                            <TabsTrigger value="DHOR">
                                                {t("tabs.DHOR")}
                                            </TabsTrigger>
                                        </TabsList>
                                        <TabsContent value="SABAK">
                                            <QuranFlowForm
                                                key={`sabak-${studentId}`}
                                                flow="SABAK"
                                                courseId={courseId}
                                                sessionId={sessionId}
                                                studentId={studentId}
                                            />
                                        </TabsContent>
                                        <TabsContent value="SABQI">
                                            <QuranFlowForm
                                                key={`sabqi-${studentId}`}
                                                flow="SABQI"
                                                courseId={courseId}
                                                sessionId={sessionId}
                                                studentId={studentId}
                                            />
                                        </TabsContent>
                                        <TabsContent value="DHOR">
                                            <QuranFlowForm
                                                key={`dhor-${studentId}`}
                                                flow="DHOR"
                                                courseId={courseId}
                                                sessionId={sessionId}
                                                studentId={studentId}
                                            />
                                        </TabsContent>
                                    </Tabs>
                                )}

                                {course.type === "QAIDA" && (
                                    <LessonFlowForm
                                        key={`lesson-${studentId}`}
                                        courseId={courseId}
                                        sessionId={sessionId}
                                        studentId={studentId}
                                        lessons={course.qaidaLessons ?? []}
                                    />
                                )}

                                {course.type === "BOOK" && (
                                    <ChapterFlowForm
                                        key={`chapter-${studentId}`}
                                        courseId={courseId}
                                        sessionId={sessionId}
                                        studentId={studentId}
                                    />
                                )}
                            </>
                        )}
                    </div>
                )}
            </DialogContent>
        </Dialog>
    );
}
