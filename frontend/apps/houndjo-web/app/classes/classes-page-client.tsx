"use client";

import dynamic from "next/dynamic";

const ClassesClient = dynamic(() => import("./classes-client"), {
    ssr: false,
});

export default function ClassesPageClient() {
    return <ClassesClient />;
}
