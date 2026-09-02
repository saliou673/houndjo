"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { Settings } from "@/features/settings";
import { SettingsDisplay } from "@/features/settings/display";

export default function SettingsDisplayClient() {
    return (
        <AuthenticatedLayout>
            <Settings>
                <SettingsDisplay />
            </Settings>
        </AuthenticatedLayout>
    );
}
