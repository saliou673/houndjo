"use client";

import { AuthenticatedLayout } from "@/components/layout/authenticated-layout";
import { Configurations } from "@/features/configurations";
import { ConfigurationsLayout } from "@/features/configurations/layout";

export default function ConfigurationsClient() {
    return (
        <AuthenticatedLayout>
            <ConfigurationsLayout>
                <Configurations />
            </ConfigurationsLayout>
        </AuthenticatedLayout>
    );
}
