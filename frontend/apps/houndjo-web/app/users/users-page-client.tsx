"use client";

import dynamic from "next/dynamic";

const UsersClient = dynamic(() => import("./users-client"), { ssr: false });

export default function UsersPageClient() {
    return <UsersClient />;
}
