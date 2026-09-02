"use client";

import { useTranslations } from "next-intl";
import { useGetCurrentUserPermissions } from "@api-client";
import { Main } from "@/components/layout/main";
import { RoleGroupsDialogs } from "./components/role-groups-dialogs";
import { RoleGroupsPrimaryButtons } from "./components/role-groups-primary-buttons";
import { RoleGroupsProvider } from "./components/role-groups-provider";
import { RoleGroupsTable } from "./components/role-groups-table";

export function RoleGroups() {
    const t = useTranslations("RoleGroups");
    const { data: permissions } = useGetCurrentUserPermissions();

    const permissionCodes = new Set(
        (permissions ?? [])
            .map((permission) => permission.code)
            .filter((code): code is string => typeof code === "string")
    );

    const canManageRoleGroups = permissionCodes.has("role-group:manage");

    return (
        <RoleGroupsProvider>
            <Main className="flex flex-1 flex-col gap-4 sm:gap-6">
                <div className="flex flex-wrap items-end justify-between gap-2">
                    <div>
                        <h2 className="text-2xl font-bold tracking-tight">
                            {t("title")}
                        </h2>
                        <p className="text-muted-foreground">
                            {t("description")}
                        </p>
                    </div>
                    <RoleGroupsPrimaryButtons
                        canManageRoleGroups={canManageRoleGroups}
                    />
                </div>
                <RoleGroupsTable canManageRoleGroups={canManageRoleGroups} />
            </Main>

            <RoleGroupsDialogs canManageRoleGroups={canManageRoleGroups} />
        </RoleGroupsProvider>
    );
}
