"use client";

import { ComingSoon } from "@/components/coming-soon";
import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";

export default function HelpCenterClient() {
    return (
        <AuthenticatedLayout>
            <ComingSoon />
        </AuthenticatedLayout>
    );
}
