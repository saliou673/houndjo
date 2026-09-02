import { useMemo } from "react";
import { useGetCurrentUserPermissions } from "@api-client";
import { useTranslations } from "next-intl";
import { useSession } from "next-auth/react";
import { useLayout } from "@/context/layout-provider";
import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarHeader,
    SidebarRail,
} from "@/components/ui/sidebar";
import { getSidebarNavGroups, sidebarTeams } from "./data/sidebar-data";
import { NavGroup } from "./nav-group";
import { NavUser } from "./nav-user";
import { TeamSwitcher } from "./team-switcher";
import {
    type NavCollapsible,
    type NavGroup as NavGroupType,
    type NavItem,
} from "./types";

type NavSubItem = NavCollapsible["items"][number];

function canAccessItem(
    item: { requiredPermission?: string },
    permissionCodes: Set<string>
) {
    return (
        !item.requiredPermission || permissionCodes.has(item.requiredPermission)
    );
}

function filterNavSubItems(
    items: NavSubItem[],
    permissionCodes: Set<string>
): NavSubItem[] {
    return items.filter((item) => canAccessItem(item, permissionCodes));
}

function filterNavItems(
    items: NavItem[],
    permissionCodes: Set<string>
): NavItem[] {
    const filteredItems: NavItem[] = [];

    for (const item of items) {
        if (!canAccessItem(item, permissionCodes)) {
            continue;
        }

        if (!item.items) {
            filteredItems.push(item);
            continue;
        }

        const filteredChildren = filterNavSubItems(item.items, permissionCodes);

        if (filteredChildren.length === 0) {
            continue;
        }

        filteredItems.push({ ...item, items: filteredChildren });
    }

    return filteredItems;
}

function filterNavGroups(
    navGroups: NavGroupType[],
    permissionCodes: Set<string>
): NavGroupType[] {
    const filteredGroups: NavGroupType[] = [];

    for (const group of navGroups) {
        const filteredItems = filterNavItems(group.items, permissionCodes);

        if (filteredItems.length === 0) {
            continue;
        }

        filteredGroups.push({ ...group, items: filteredItems });
    }

    return filteredGroups;
}

export function AppSidebar() {
    const { collapsible, variant } = useLayout();
    const { status } = useSession();
    const t = useTranslations("Sidebar");
    const tSettingsNav = useTranslations("Settings.nav");
    const { data: permissions } = useGetCurrentUserPermissions(undefined, {
        query: {
            enabled: status === "authenticated",
        },
    });
    const permissionCodes = useMemo(
        () =>
            new Set(
                (permissions ?? [])
                    .map((permission) => permission.code)
                    .filter((code): code is string => Boolean(code))
            ),
        [permissions]
    );
    const navGroups = useMemo(
        () =>
            filterNavGroups(
                getSidebarNavGroups(t, tSettingsNav),
                permissionCodes
            ),
        [permissionCodes, t, tSettingsNav]
    );

    return (
        <Sidebar collapsible={collapsible} variant={variant}>
            <SidebarHeader>
                <TeamSwitcher teams={sidebarTeams} />

                {/* Replace <TeamSwitch /> with the following <AppTitle />
         /* if you want to use the normal app title instead of TeamSwitch dropdown */}
            </SidebarHeader>
            <SidebarContent>
                {navGroups.map((props) => (
                    <NavGroup key={props.title} {...props} />
                ))}
            </SidebarContent>
            <SidebarFooter>
                <NavUser />
            </SidebarFooter>
            <SidebarRail />
        </Sidebar>
    );
}
