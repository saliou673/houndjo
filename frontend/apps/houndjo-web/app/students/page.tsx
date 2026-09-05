import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import { requirePermission } from "@/lib/server/require-permission";
import StudentsPageClient from "./students-page-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("Students");
    return {
        title: t("pageTitle"),
    };
}

export default async function StudentsPage() {
    await requirePermission("student:read");

    return <StudentsPageClient />;
}
