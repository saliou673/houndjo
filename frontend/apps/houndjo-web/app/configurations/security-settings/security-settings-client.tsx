"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { ConfigurationsLayout } from "@/features/configurations/layout";
import { SecuritySettingsFeature } from "@/features/security-settings";

export default function SecuritySettingsClient() {
    return (
        <AuthenticatedLayout>
            <ConfigurationsLayout>
                <SecuritySettingsFeature />
            </ConfigurationsLayout>
        </AuthenticatedLayout>
    );
}
