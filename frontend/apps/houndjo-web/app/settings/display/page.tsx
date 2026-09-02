import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import SettingsDisplayClient from "./settings-display-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("SettingsDisplay");
    return {
        title: t("metadataTitle"),
    };
}

export default function SettingsDisplayPage() {
    return <SettingsDisplayClient />;
}
