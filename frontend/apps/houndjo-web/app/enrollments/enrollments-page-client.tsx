"use client";

import dynamic from "next/dynamic";

const EnrollmentsClient = dynamic(() => import("./enrollments-client"), {
    ssr: false,
});

export default function EnrollmentsPageClient() {
    return <EnrollmentsClient />;
}
