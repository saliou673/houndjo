"use client";

import { useState } from "react";
import { useGetCurrentUserPermissions } from "@api-client";
import { Plus, Search } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Main } from "@/components/layout/main";
import { StudentDeleteDialog } from "./components/student-delete-dialog";
import { StudentFormDialog } from "./components/student-form-dialog";
import { StudentList } from "./components/student-list";
import { type StudentRow } from "./data/schema";

export function Students() {
    const t = useTranslations("Students");
    const { data: permissions } = useGetCurrentUserPermissions();

    const permissionCodes = new Set(
        (permissions ?? [])
            .map((permission) => permission.code)
            .filter((code): code is string => typeof code === "string")
    );
    const canCreateStudents = permissionCodes.has("student:create");
    const canUpdateStudents = permissionCodes.has("student:update");
    const canDeleteStudents = permissionCodes.has("student:delete");

    const [search, setSearch] = useState("");
    const [addOpen, setAddOpen] = useState(false);
    const [editRow, setEditRow] = useState<StudentRow | null>(null);
    const [deleteRow, setDeleteRow] = useState<StudentRow | null>(null);

    return (
        <Main className="flex flex-1 flex-col gap-4 sm:gap-6">
            <div className="flex flex-wrap items-end justify-between gap-2">
                <div>
                    <h2 className="text-2xl font-bold tracking-tight">
                        {t("title")}
                    </h2>
                    <p className="text-muted-foreground">{t("description")}</p>
                </div>
                {canCreateStudents && (
                    <Button
                        className="space-x-1"
                        onClick={() => setAddOpen(true)}
                    >
                        <span>{t("addStudent")}</span> <Plus size={18} />
                    </Button>
                )}
            </div>

            <div className="relative max-w-sm">
                <Search className="absolute start-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder={t("searchPlaceholder")}
                    className="ps-9"
                />
            </div>

            <StudentList
                search={search}
                canUpdate={canUpdateStudents}
                canDelete={canDeleteStudents}
                onEdit={setEditRow}
                onDelete={setDeleteRow}
            />

            {canCreateStudents && (
                <StudentFormDialog open={addOpen} onOpenChange={setAddOpen} />
            )}

            {editRow && canUpdateStudents && (
                <StudentFormDialog
                    key={`student-edit-${editRow.id}`}
                    open={!!editRow}
                    currentRow={editRow}
                    onOpenChange={(open) => !open && setEditRow(null)}
                />
            )}

            {deleteRow && canDeleteStudents && (
                <StudentDeleteDialog
                    key={`student-delete-${deleteRow.id}`}
                    open={!!deleteRow}
                    currentRow={deleteRow}
                    onOpenChange={(open) => !open && setDeleteRow(null)}
                />
            )}
        </Main>
    );
}
