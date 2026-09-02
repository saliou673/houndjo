import type { Metadata } from "next";
import { requirePermission } from "@/lib/server/require-permission";
import SecuritySettingsClient from "./security-settings-client";

export const metadata: Metadata = {
    title: "Security Settings",
};

export default async function SecuritySettingsPage() {
    await requirePermission("config:manage");

    return <SecuritySettingsClient />;
}
