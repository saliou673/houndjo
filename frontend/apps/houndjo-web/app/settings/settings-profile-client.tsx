"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { Settings } from "@/features/settings";
import { SettingsProfile } from "@/features/settings/profile";

export default function SettingsProfileClient() {
    return (
        <AuthenticatedLayout>
            <Settings>
                <SettingsProfile />
            </Settings>
        </AuthenticatedLayout>
    );
}
