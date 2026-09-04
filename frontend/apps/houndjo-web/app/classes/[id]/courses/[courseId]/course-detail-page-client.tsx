"use client";

import dynamic from "next/dynamic";

const CourseDetailClient = dynamic(() => import("./course-detail-client"), {
    ssr: false,
});

type CourseDetailPageClientProps = {
    classId: number;
    courseId: number;
};

export default function CourseDetailPageClient({
    classId,
    courseId,
}: CourseDetailPageClientProps) {
    return <CourseDetailClient classId={classId} courseId={courseId} />;
}
