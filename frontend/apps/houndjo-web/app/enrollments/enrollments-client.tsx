"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { Enrollments } from "@/features/enrollments";

export default function EnrollmentsClient() {
    return (
        <AuthenticatedLayout>
            <Enrollments />
        </AuthenticatedLayout>
    );
}
