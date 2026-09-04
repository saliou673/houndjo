"use client";

import { useState } from "react";
import { useGetCurrentUserPermissions } from "@api-client";
import { Plus } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Main } from "@/components/layout/main";
import { ClassDeleteDialog } from "./components/class-delete-dialog";
import { ClassFormDialog } from "./components/class-form-dialog";
import { ClassList } from "./components/class-list";
import { type ClassRow } from "./data/schema";

export function Classes() {
    const t = useTranslations("Classes");
    const { data: permissions } = useGetCurrentUserPermissions();

    const permissionCodes = new Set(
        (permissions ?? [])
            .map((permission) => permission.code)
            .filter((code): code is string => typeof code === "string")
    );
    const canCreateClasses = permissionCodes.has("class:create");
    const canUpdateClasses = permissionCodes.has("class:update");
    const canDeleteClasses = permissionCodes.has("class:delete");

    const [addOpen, setAddOpen] = useState(false);
    const [editRow, setEditRow] = useState<ClassRow | null>(null);
    const [deleteRow, setDeleteRow] = useState<ClassRow | null>(null);

    return (
        <Main className="flex flex-1 flex-col gap-4 sm:gap-6">
            <div className="flex flex-wrap items-end justify-between gap-2">
                <div>
                    <h2 className="text-2xl font-bold tracking-tight">
                        {t("title")}
                    </h2>
                    <p className="text-muted-foreground">{t("description")}</p>
                </div>
                {canCreateClasses && (
                    <Button
                        className="space-x-1"
                        onClick={() => setAddOpen(true)}
                    >
                        <span>{t("addClass")}</span> <Plus size={18} />
                    </Button>
                )}
            </div>

            <ClassList
                canUpdate={canUpdateClasses}
                canDelete={canDeleteClasses}
                onEdit={setEditRow}
                onDelete={setDeleteRow}
            />

            {canCreateClasses && (
                <ClassFormDialog open={addOpen} onOpenChange={setAddOpen} />
            )}

            {editRow && canUpdateClasses && (
                <ClassFormDialog
                    key={`class-edit-${editRow.id}`}
                    open={!!editRow}
                    currentRow={editRow}
                    onOpenChange={(open) => !open && setEditRow(null)}
                />
            )}

            {deleteRow && canDeleteClasses && (
                <ClassDeleteDialog
                    key={`class-delete-${deleteRow.id}`}
                    open={!!deleteRow}
                    currentRow={deleteRow}
                    onOpenChange={(open) => !open && setDeleteRow(null)}
                />
            )}
        </Main>
    );
}
