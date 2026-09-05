"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { CourseDetail } from "@/features/academics/course-detail";

type CourseDetailClientProps = {
    classId: number;
    courseId: number;
};

export default function CourseDetailClient({
    classId,
    courseId,
}: CourseDetailClientProps) {
    return (
        <AuthenticatedLayout>
            <CourseDetail classId={classId} courseId={courseId} />
        </AuthenticatedLayout>
    );
}
