"use client";

import { Monitor, Bell, Palette, Wrench, UserCog } from "lucide-react";
import { useTranslations } from "next-intl";
import { Separator } from "@/components/ui/separator";
import { Main } from "@/components/layout/main";
import { SidebarNav } from "./components/sidebar-nav";

type SettingsProps = {
    children?: React.ReactNode;
};

export function Settings({ children }: SettingsProps) {
    const t = useTranslations("Settings");

    const sidebarNavItems = [
        {
            title: t("nav.profile"),
            href: "/settings",
            icon: <UserCog size={18} />,
        },
        {
            title: t("nav.account"),
            href: "/settings/account",
            icon: <Wrench size={18} />,
        },
        {
            title: t("nav.appearance"),
            href: "/settings/appearance",
            icon: <Palette size={18} />,
        },
        {
            title: t("nav.notifications"),
            href: "/settings/notifications",
            icon: <Bell size={18} />,
        },
        {
            title: t("nav.display"),
            href: "/settings/display",
            icon: <Monitor size={18} />,
        },
    ];

    return (
        <>
            <Main fixed>
                <div className="space-y-0.5">
                    <h1 className="text-2xl font-bold tracking-tight md:text-3xl">
                        {t("pageTitle")}
                    </h1>
                    <p className="text-muted-foreground">
                        {t("description")}
                    </p>
                </div>
                <Separator className="my-4 lg:my-6" />
                <div className="flex flex-1 flex-col space-y-2 overflow-hidden md:space-y-2 lg:flex-row lg:space-y-0 lg:space-x-12">
                    <aside className="top-0 lg:sticky lg:w-1/5">
                        <SidebarNav items={sidebarNavItems} />
                    </aside>
                    <div className="flex w-full overflow-y-hidden p-1">
                        {children ?? null}
                    </div>
                </div>
            </Main>
        </>
    );
}
