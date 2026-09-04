"use client";

import { useState } from "react";
import { useGetClassById, useGetCurrentUserPermissions } from "@api-client";
import { Plus } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Main } from "@/components/layout/main";
import { CourseDeleteDialog } from "./components/course-delete-dialog";
import { CourseFormDialog } from "./components/course-form-dialog";
import { CourseList } from "./components/course-list";
import { type CourseRow } from "./data/schema";

type ClassDetailProps = {
    classId: number;
};

export function ClassDetail({ classId }: ClassDetailProps) {
    const t = useTranslations("Classes.detail");
    const { data: permissions } = useGetCurrentUserPermissions();
    const { data: schoolClass, isLoading, isError } = useGetClassById(classId);

    const permissionCodes = new Set(
        (permissions ?? [])
            .map((permission) => permission.code)
            .filter((code): code is string => typeof code === "string")
    );
    const canCreateCourses = permissionCodes.has("course:create");
    const canUpdateCourses = permissionCodes.has("course:update");
    const canDeleteCourses = permissionCodes.has("course:delete");

    const [addOpen, setAddOpen] = useState(false);
    const [editRow, setEditRow] = useState<CourseRow | null>(null);
    const [deleteRow, setDeleteRow] = useState<CourseRow | null>(null);

    if (isLoading) {
        return (
            <Main>
                <p className="text-sm text-muted-foreground">{t("loading")}</p>
            </Main>
        );
    }

    if (isError || !schoolClass) {
        return (
            <Main>
                <p className="text-sm text-destructive">{t("errorFallback")}</p>
            </Main>
        );
    }

    return (
        <Main className="flex flex-1 flex-col gap-4 sm:gap-6">
            <div>
                <h2 className="text-2xl font-bold tracking-tight">
                    {schoolClass.name}
                </h2>
                {schoolClass.description && (
                    <p className="text-muted-foreground">
                        {schoolClass.description}
                    </p>
                )}
            </div>

            <div className="flex flex-wrap items-end justify-between gap-2">
                <h3 className="text-lg font-semibold">{t("coursesTitle")}</h3>
                {canCreateCourses && (
                    <Button
                        className="space-x-1"
                        onClick={() => setAddOpen(true)}
                    >
                        <span>{t("addCourse")}</span> <Plus size={18} />
                    </Button>
                )}
            </div>

            <CourseList
                classId={classId}
                canUpdate={canUpdateCourses}
                canDelete={canDeleteCourses}
                onEdit={setEditRow}
                onDelete={setDeleteRow}
            />

            {canCreateCourses && (
                <CourseFormDialog
                    classId={classId}
                    open={addOpen}
                    onOpenChange={setAddOpen}
                />
            )}

            {editRow && canUpdateCourses && (
                <CourseFormDialog
                    key={`course-edit-${editRow.id}`}
                    classId={classId}
                    open={!!editRow}
                    currentRow={editRow}
                    onOpenChange={(open) => !open && setEditRow(null)}
                />
            )}

            {deleteRow && canDeleteCourses && (
                <CourseDeleteDialog
                    key={`course-delete-${deleteRow.id}`}
                    classId={classId}
                    open={!!deleteRow}
                    currentRow={deleteRow}
                    onOpenChange={(open) => !open && setDeleteRow(null)}
                />
            )}
        </Main>
    );
}
