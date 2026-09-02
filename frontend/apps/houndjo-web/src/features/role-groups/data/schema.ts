import { type RoleGroup } from "@api-client";

export type RoleGroupRow = {
    id: number;
    name: string;
    description: string | null;
    permissionCodes: string[];
    creationDate: string | null;
    lastUpdateDate: string | null;
};

export function mapRoleGroupToRow(roleGroup: RoleGroup): RoleGroupRow {
    return {
        id: roleGroup.id ?? 0,
        name: roleGroup.name ?? "",
        description: roleGroup.description ?? null,
        permissionCodes: (roleGroup.permissions ?? [])
            .map((p) => p.code)
            .filter((code): code is string => !!code)
            .sort(),
        creationDate: roleGroup.creationDate ?? null,
        lastUpdateDate: roleGroup.lastUpdateDate ?? null,
    };
}
