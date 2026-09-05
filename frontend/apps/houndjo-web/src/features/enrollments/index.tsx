"use client";

import { useState } from "react";
import { useGetCurrentUserPermissions } from "@api-client";
import { Plus } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Main } from "@/components/layout/main";
import { type EnrollmentRow } from "./data/schema";
import { EnrollmentCoursesDialog } from "./components/enrollment-courses-dialog";
import { EnrollmentEndDialog } from "./components/enrollment-end-dialog";
import { EnrollmentFiltersBar, type EnrollmentFilters } from "./components/enrollment-filters";
import { EnrollmentFormDialog } from "./components/enrollment-form-dialog";
import { EnrollmentList } from "./components/enrollment-list";

export function Enrollments() {
    const t = useTranslations("Enrollments");
    const { data: permissions } = useGetCurrentUserPermissions();

    const permissionCodes = new Set(
        (permissions ?? [])
            .map((permission) => permission.code)
            .filter((code): code is string => typeof code === "string")
    );
    const canReadClasses = permissionCodes.has("class:read");
    const canReadStudents = permissionCodes.has("student:read");
    const canReadCourses = permissionCodes.has("course:read");
    const canCreateEnrollments =
        permissionCodes.has("enrollment:create") && canReadClasses && canReadStudents;
    const canEndEnrollments = permissionCodes.has("enrollment:update");
    const canManageCourses = canEndEnrollments && canReadCourses;

    const [filters, setFilters] = useState<EnrollmentFilters>({});
    const [addOpen, setAddOpen] = useState(false);
    const [coursesRow, setCoursesRow] = useState<EnrollmentRow | null>(null);
    const [endRow, setEndRow] = useState<EnrollmentRow | null>(null);

    return (
        <Main className="flex flex-1 flex-col gap-4 sm:gap-6">
            <div className="flex flex-wrap items-end justify-between gap-2">
                <div>
                    <h2 className="text-2xl font-bold tracking-tight">
                        {t("title")}
                    </h2>
                    <p className="text-muted-foreground">{t("description")}</p>
                </div>
                {canCreateEnrollments && (
                    <Button
                        className="space-x-1"
                        onClick={() => setAddOpen(true)}
                    >
                        <span>{t("addEnrollment")}</span> <Plus size={18} />
                    </Button>
                )}
            </div>

            <EnrollmentFiltersBar
                filters={filters}
                canReadClasses={canReadClasses}
                canReadStudents={canReadStudents}
                onChange={setFilters}
            />

            <EnrollmentList
                filters={filters}
                canManageCourses={canManageCourses}
                canEnd={canEndEnrollments}
                onManageCourses={setCoursesRow}
                onEnd={setEndRow}
            />

            {canCreateEnrollments && (
                <EnrollmentFormDialog
                    open={addOpen}
                    canReadCourses={canReadCourses}
                    onOpenChange={setAddOpen}
                />
            )}

            {coursesRow && canManageCourses && (
                <EnrollmentCoursesDialog
                    key={`enrollment-courses-${coursesRow.id}`}
                    open={!!coursesRow}
                    currentRow={coursesRow}
                    onOpenChange={(open) => !open && setCoursesRow(null)}
                />
            )}

            {endRow && canEndEnrollments && (
                <EnrollmentEndDialog
                    key={`enrollment-end-${endRow.id}`}
                    open={!!endRow}
                    currentRow={endRow}
                    onOpenChange={(open) => !open && setEndRow(null)}
                />
            )}
        </Main>
    );
}
