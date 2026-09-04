"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { ClassDetail } from "@/features/academics/class-detail";

type ClassDetailClientProps = {
    classId: number;
};

export default function ClassDetailClient({ classId }: ClassDetailClientProps) {
    return (
        <AuthenticatedLayout>
            <ClassDetail classId={classId} />
        </AuthenticatedLayout>
    );
}
