import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import SettingsAppearanceClient from "./settings-appearance-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("SettingsAppearance");
    return {
        title: t("metadataTitle"),
    };
}

export default function SettingsAppearancePage() {
    return <SettingsAppearanceClient />;
}
