"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { Users } from "@/features/users";

export default function UsersClient() {
    return (
        <AuthenticatedLayout>
            <Users />
        </AuthenticatedLayout>
    );
}
