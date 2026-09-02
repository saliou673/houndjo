import {
    type RoleGroup,
    type UserDetails,
    type UserDetailsGenderEnumKey,
    type UserDetailsStatusEnumKey,
} from "@api-client";

export type UserGender = UserDetailsGenderEnumKey;
export type UserStatus = NonNullable<UserDetailsStatusEnumKey>;

export type UserRow = {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    fullName: string;
    phoneNumber: string | null;
    birthDate: string;
    gender: UserGender;
    address: string | null;
    status: UserStatus;
    languageKey: string | null;
    imageUrl: string | null;
    permissions: string[];
    roleGroupNames: string[];
    creationDate: string | null;
    lastUpdateDate: string | null;
};

export type RoleGroupOption = {
    id: number;
    name: string;
    description: string | null;
};

export function mapUserDetailsToRow(user: UserDetails): UserRow {
    return {
        id: user.id ?? 0,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        fullName: `${user.firstName} ${user.lastName}`.trim(),
        phoneNumber: user.phoneNumber ?? null,
        birthDate: user.birthDate,
        gender: user.gender,
        address: user.address ?? null,
        status: user.status ?? "NOT_ACTIVATED",
        languageKey: user.languageKey ?? null,
        imageUrl: user.imageUrl ?? null,
        permissions: user.permissions,
        roleGroupNames: (user.roleGroups ?? [])
            .map((rg) => rg.name)
            .filter((name): name is string => !!name)
            .sort(),
        creationDate: user.creationDate ?? null,
        lastUpdateDate: user.lastUpdateDate ?? null,
    };
}

export function mapRoleGroupToOption(
    roleGroup: RoleGroup
): RoleGroupOption | null {
    if (!roleGroup.id || !roleGroup.name) {
        return null;
    }

    return {
        id: roleGroup.id,
        name: roleGroup.name,
        description: roleGroup.description ?? null,
    };
}
