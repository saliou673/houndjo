import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import SettingsNotificationsClient from "./settings-notifications-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("SettingsNotifications");
    return {
        title: t("metadataTitle"),
    };
}

export default function SettingsNotificationsPage() {
    return <SettingsNotificationsClient />;
}
