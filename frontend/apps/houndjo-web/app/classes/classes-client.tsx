"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { Classes } from "@/features/academics";

export default function ClassesClient() {
    return (
        <AuthenticatedLayout>
            <Classes />
        </AuthenticatedLayout>
    );
}
