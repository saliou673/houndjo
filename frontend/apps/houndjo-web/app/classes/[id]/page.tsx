import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import { requirePermission } from "@/lib/server/require-permission";
import ClassDetailPageClient from "./class-detail-page-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("Classes.detail");
    return {
        title: t("pageTitle"),
    };
}

type ClassDetailPageProps = {
    params: Promise<{ id: string }>;
};

export default async function ClassDetailPage({ params }: ClassDetailPageProps) {
    await requirePermission("class:read");
    const { id } = await params;

    return <ClassDetailPageClient classId={Number(id)} />;
}
