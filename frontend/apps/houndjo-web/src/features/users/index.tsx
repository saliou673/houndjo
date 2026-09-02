"use client";

import { useTranslations } from "next-intl";
import { useGetCurrentUserPermissions } from "@api-client";
import { Main } from "@/components/layout/main";
import { UsersDialogs } from "./components/users-dialogs";
import { UsersPrimaryButtons } from "./components/users-primary-buttons";
import { UsersProvider } from "./components/users-provider";
import { UsersTable } from "./components/users-table";

export function Users() {
    const t = useTranslations("Users");
    const { data: permissions } = useGetCurrentUserPermissions();

    const permissionCodes = new Set(
        (permissions ?? [])
            .map((permission) => permission.code)
            .filter((code): code is string => typeof code === "string")
    );
    const canCreateUsers =
        permissionCodes.has("user:create") &&
        permissionCodes.has("role-group:read");
    const canUpdateUsers = permissionCodes.has("user:update");
    const canDeleteUsers = permissionCodes.has("user:deactivate");

    return (
        <UsersProvider>
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
                    <UsersPrimaryButtons canCreateUsers={canCreateUsers} />
                </div>
                <UsersTable
                    canDeleteUsers={canDeleteUsers}
                    canUpdateUsers={canUpdateUsers}
                />
            </Main>

            <UsersDialogs
                canCreateUsers={canCreateUsers}
                canUpdateUsers={canUpdateUsers}
                canDeleteUsers={canDeleteUsers}
            />
        </UsersProvider>
    );
}
