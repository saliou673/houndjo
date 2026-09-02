"use client";

import { ShieldAlert, Tag } from "lucide-react";
import { Separator } from "@/components/ui/separator";
import { Main } from "@/components/layout/main";
import { SidebarNav } from "@/features/settings/components/sidebar-nav";

const sidebarNavItems = [
    {
        title: "Reference Data",
        href: "/configurations",
        icon: <Tag size={18} />,
    },
    {
        title: "Security",
        href: "/configurations/security-settings",
        icon: <ShieldAlert size={18} />,
    },
];

type ConfigurationsLayoutProps = {
    children?: React.ReactNode;
};

export function ConfigurationsLayout({ children }: ConfigurationsLayoutProps) {
    return (
        <Main fixed>
            <div className="space-y-0.5">
                <h1 className="text-2xl font-bold tracking-tight md:text-3xl">
                    Configuration
                </h1>
                <p className="text-muted-foreground">
                    Manage application-wide settings and reference data.
                </p>
            </div>
            <Separator className="my-4 lg:my-6" />
            <div className="flex flex-1 flex-col space-y-2 overflow-hidden md:space-y-2 lg:flex-row lg:space-y-0 lg:space-x-12">
                <aside className="top-0 lg:sticky lg:w-1/5">
                    <SidebarNav items={sidebarNavItems} />
                </aside>
                <div className="flex w-full overflow-y-auto p-1">
                    {children ?? null}
                </div>
            </div>
        </Main>
    );
}
