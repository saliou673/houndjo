"use client";

import dynamic from "next/dynamic";

const RoleGroupsClient = dynamic(() => import("./role-groups-client"), {
    ssr: false,
});

export default function RoleGroupsPageClient() {
    return <RoleGroupsClient />;
}
