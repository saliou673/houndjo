import type { Metadata } from "next";
import { getTranslations } from "next-intl/server";
import SettingsAccountClient from "./settings-account-client";

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations("SettingsAccount");
    return {
        title: t("metadataTitle"),
    };
}

export default function SettingsAccountPage() {
    return <SettingsAccountClient />;
}
