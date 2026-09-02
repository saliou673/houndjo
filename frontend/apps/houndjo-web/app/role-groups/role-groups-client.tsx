"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { RoleGroups } from "@/features/role-groups";

export default function RoleGroupsClient() {
    return (
        <AuthenticatedLayout>
            <RoleGroups />
        </AuthenticatedLayout>
    );
}
