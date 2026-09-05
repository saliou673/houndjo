"use client";

import dynamic from "next/dynamic";

const StudentsClient = dynamic(() => import("./students-client"), {
    ssr: false,
});

export default function StudentsPageClient() {
    return <StudentsClient />;
}
