import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import SettingsProfileClient from "./settings-profile-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("SettingsProfile");
    return {
        title: t("pageTitle"),
    };
}

export default function SettingsPage() {
    return <SettingsProfileClient />;
}
