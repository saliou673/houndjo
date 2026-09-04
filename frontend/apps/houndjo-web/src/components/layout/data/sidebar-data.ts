import {
    LayoutDashboard,
    Monitor,
    HelpCircle,
    Bell,
    Palette,
    Settings,
    Wrench,
    UserCog,
    Users,
    ShieldCheck,
    AudioWaveform,
    Command,
    GalleryVerticalEnd,
    SlidersHorizontal,
    Tag,
    ShieldAlert,
    GraduationCap,
    UserRound,
    ClipboardList,
} from "lucide-react";
import { type useTranslations } from "next-intl";
import { type SidebarData } from "../types";

export const sidebarTeams: SidebarData["teams"] = [
    {
        name: "Shadcn Admin",
        logo: Command,
        plan: "Vite + ShadcnUI",
    },
    {
        name: "Acme Inc",
        logo: GalleryVerticalEnd,
        plan: "Enterprise",
    },
    {
        name: "Acme Corp.",
        logo: AudioWaveform,
        plan: "Startup",
    },
];

export function getSidebarNavGroups(
    t: ReturnType<typeof useTranslations>,
    tSettingsNav: ReturnType<typeof useTranslations>
): SidebarData["navGroups"] {
    return [
        {
            title: t("groups.general"),
            items: [
                {
                    title: t("nav.dashboard"),
                    url: "/",
                    icon: LayoutDashboard,
                },
                {
                    title: t("nav.users"),
                    url: "/users",
                    icon: Users,
                    requiredPermission: "user:read",
                },
                {
                    title: t("nav.roleGroups"),
                    url: "/role-groups",
                    icon: ShieldCheck,
                    requiredPermission: "role-group:read",
                },
                {
                    title: t("nav.classes"),
                    url: "/classes",
                    icon: GraduationCap,
                    requiredPermission: "class:read",
                },
                {
                    title: t("nav.students"),
                    url: "/students",
                    icon: UserRound,
                    requiredPermission: "student:read",
                },
                {
                    title: t("nav.enrollments"),
                    url: "/enrollments",
                    icon: ClipboardList,
                    requiredPermission: "enrollment:read",
                },
                {
                    title: t("nav.configuration"),
                    icon: SlidersHorizontal,
                    requiredPermission: "config:manage",
                    items: [
                        {
                            title: t("nav.referenceData"),
                            url: "/configurations",
                            icon: Tag,
                        },
                        {
                            title: t("nav.security"),
                            url: "/configurations/security-settings",
                            icon: ShieldAlert,
                        },
                    ],
                },
            ],
        },
        {
            title: t("groups.other"),
            items: [
                {
                    title: t("nav.settings"),
                    icon: Settings,
                    items: [
                        {
                            title: tSettingsNav("profile"),
                            url: "/settings",
                            icon: UserCog,
                        },
                        {
                            title: tSettingsNav("account"),
                            url: "/settings/account",
                            icon: Wrench,
                        },
                        {
                            title: tSettingsNav("appearance"),
                            url: "/settings/appearance",
                            icon: Palette,
                        },
                        {
                            title: tSettingsNav("notifications"),
                            url: "/settings/notifications",
                            icon: Bell,
                        },
                        {
                            title: tSettingsNav("display"),
                            url: "/settings/display",
                            icon: Monitor,
                        },
                    ],
                },
                {
                    title: t("nav.helpCenter"),
                    url: "/help-center",
                    icon: HelpCircle,
                },
            ],
        },
    ];
}
