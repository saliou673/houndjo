"use client";

import { useState } from "react";
import { useGetCourseById, useGetCurrentUserPermissions } from "@api-client";
import { Plus, RefreshCw } from "lucide-react";
import { useTranslations } from "next-intl";
import Link from "next/link";
import { allowedActions } from "@/lib/allowed-actions";
import { Button } from "@/components/ui/button";
import { Main } from "@/components/layout/main";
import { PaceEditor } from "./components/pace-editor";
import { SessionFormDialog } from "./components/session-form-dialog";
import { SessionGenerateDialog } from "./components/session-generate-dialog";
import { SessionList } from "./components/session-list";

type CourseDetailProps = {
    classId: number;
    courseId: number;
};

export function CourseDetail({ classId, courseId }: CourseDetailProps) {
    const t = useTranslations("Classes.courseDetail");
    const { data: permissions } = useGetCurrentUserPermissions();
    const {
        data: course,
        isLoading,
        isError,
    } = useGetCourseById(classId, courseId);

    const permissionCodes = new Set(
        (permissions ?? [])
            .map((permission) => permission.code)
            .filter((code): code is string => typeof code === "string")
    );
    const canUpdateCourse = permissionCodes.has("course:update");
    const canReadSession = permissionCodes.has("session:read");
    const canCreateSession = permissionCodes.has("session:create");
    const canUpdateSession = permissionCodes.has("session:update");
    const canRecordProgress = permissionCodes.has("progress:create");
    const canRecordAttendance = [
        "attendance:create",
        "attendance:read",
        "enrollment:read",
    ].every((code) => permissionCodes.has(code));
    const canAccessSessions = canReadSession || canCreateSession;

    const [addSessionOpen, setAddSessionOpen] = useState(false);
    const [generateOpen, setGenerateOpen] = useState(false);

    if (isLoading) {
        return (
            <Main>
                <p className="text-sm text-muted-foreground">{t("loading")}</p>
            </Main>
        );
    }

    if (isError || !course) {
        return (
            <Main>
                <p className="text-sm text-destructive">{t("errorFallback")}</p>
            </Main>
        );
    }

    return (
        <Main className="flex flex-1 flex-col gap-4 sm:gap-6">
            <div>
                <Link
                    href={`/classes/${classId}`}
                    className="text-sm text-muted-foreground hover:underline"
                >
                    {t("backToClass")}
                </Link>
                <h2 className="text-2xl font-bold tracking-tight">
                    {course.name}
                </h2>
            </div>

            <PaceEditor
                courseId={courseId}
                courseType={course.type ?? "QAIDA"}
                canUpdate={canUpdateCourse}
            />

            {canAccessSessions && (
                <>
                    <div className="flex flex-wrap items-end justify-between gap-2">
                        <h3 className="text-lg font-semibold">
                            {t("sessionsTitle")}
                        </h3>
                        {canCreateSession && (
                            <div className="flex gap-2">
                                <Button
                                    variant="outline"
                                    className="space-x-1"
                                    onClick={() => setGenerateOpen(true)}
                                >
                                    <span>{t("generateSessions")}</span>{" "}
                                    <RefreshCw size={16} />
                                </Button>
                                <Button
                                    className="space-x-1"
                                    onClick={() => setAddSessionOpen(true)}
                                >
                                    <span>{t("addSession")}</span>{" "}
                                    <Plus size={18} />
                                </Button>
                            </div>
                        )}
                    </div>

                    {canReadSession && (
                        <SessionList
                            classId={classId}
                            courseId={courseId}
                            actions={allowedActions({
                                update: canUpdateSession,
                                progress: canRecordProgress,
                                attendance: canRecordAttendance,
                            })}
                        />
                    )}

                    {canCreateSession && (
                        <SessionFormDialog
                            courseId={courseId}
                            open={addSessionOpen}
                            onOpenChange={setAddSessionOpen}
                        />
                    )}

                    {canCreateSession && (
                        <SessionGenerateDialog
                            courseId={courseId}
                            open={generateOpen}
                            onOpenChange={setGenerateOpen}
                        />
                    )}
                </>
            )}
        </Main>
    );
}
