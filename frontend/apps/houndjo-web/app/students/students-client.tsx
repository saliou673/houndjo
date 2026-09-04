"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { Students } from "@/features/students";

export default function StudentsClient() {
    return (
        <AuthenticatedLayout>
            <Students />
        </AuthenticatedLayout>
    );
}
