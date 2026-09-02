"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { Settings } from "@/features/settings";
import { SettingsAccount } from "@/features/settings/account";

export default function SettingsAccountClient() {
    return (
        <AuthenticatedLayout>
            <Settings>
                <SettingsAccount />
            </Settings>
        </AuthenticatedLayout>
    );
}
