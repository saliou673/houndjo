import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import { requirePermission } from "@/lib/server/require-permission";
import ClassesPageClient from "./classes-page-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("Classes");
    return {
        title: t("pageTitle"),
    };
}

export default async function ClassesPage() {
    await requirePermission("class:read");

    return <ClassesPageClient />;
}
