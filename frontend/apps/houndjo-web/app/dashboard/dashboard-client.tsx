"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { Dashboard } from "@/features/dashboard";

export default function DashboardClient() {
    return (
        <AuthenticatedLayout header={false}>
            <Dashboard />
        </AuthenticatedLayout>
    );
}
