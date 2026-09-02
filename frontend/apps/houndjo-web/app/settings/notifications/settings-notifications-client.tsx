"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { Settings } from "@/features/settings";
import { SettingsNotifications } from "@/features/settings/notifications";

export default function SettingsNotificationsClient() {
    return (
        <AuthenticatedLayout>
            <Settings>
                <SettingsNotifications />
            </Settings>
        </AuthenticatedLayout>
    );
}
