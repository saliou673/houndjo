import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import { requirePermission } from "@/lib/server/require-permission";
import EnrollmentsPageClient from "./enrollments-page-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("Enrollments");
    return {
        title: t("pageTitle"),
    };
}

export default async function EnrollmentsPage() {
    await requirePermission("enrollment:read");

    return <EnrollmentsPageClient />;
}
