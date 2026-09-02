"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { Settings } from "@/features/settings";
import { SettingsAppearance } from "@/features/settings/appearance";

export default function SettingsAppearanceClient() {
    return (
        <AuthenticatedLayout>
            <Settings>
                <SettingsAppearance />
            </Settings>
        </AuthenticatedLayout>
    );
}
