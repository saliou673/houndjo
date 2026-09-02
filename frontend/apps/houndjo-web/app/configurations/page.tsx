import type { Metadata } from "next";
import { requirePermission } from "@/lib/server/require-permission";
import ConfigurationsClient from "./configurations-client";

export const metadata: Metadata = {
    title: "Configurations",
};

export default async function ConfigurationsPage() {
    await requirePermission("config:manage");

    return <ConfigurationsClient />;
}
