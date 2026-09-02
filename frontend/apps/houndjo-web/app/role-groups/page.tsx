import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import { requirePermission } from "@/lib/server/require-permission";
import RoleGroupsPageClient from "./role-groups-page-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("RoleGroups");
    return {
        title: t("pageTitle"),
    };
}

export default async function RoleGroupsPage() {
    await requirePermission("role-group:read");

    return <RoleGroupsPageClient />;
}
