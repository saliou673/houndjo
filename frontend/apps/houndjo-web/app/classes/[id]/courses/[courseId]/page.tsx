import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import { requirePermission } from "@/lib/server/require-permission";
import CourseDetailPageClient from "./course-detail-page-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("Classes.courseDetail");
    return {
        title: t("pageTitle"),
    };
}

type CourseDetailPageProps = {
    params: Promise<{ id: string; courseId: string }>;
};

export default async function CourseDetailPage({ params }: CourseDetailPageProps) {
    await requirePermission("course:read");
    const { id, courseId } = await params;

    return (
        <CourseDetailPageClient classId={Number(id)} courseId={Number(courseId)} />
    );
}
