import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import { requirePermission } from "@/lib/server/require-permission";
import UsersPageClient from "./users-page-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("Users");
    return {
        title: t("pageTitle"),
    };
}

export default async function UsersPage() {
    await requirePermission("user:read");

    return <UsersPageClient />;
}
