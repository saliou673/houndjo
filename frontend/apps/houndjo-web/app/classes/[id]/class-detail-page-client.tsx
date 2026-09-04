"use client";

import dynamic from "next/dynamic";

const ClassDetailClient = dynamic(() => import("./class-detail-client"), {
    ssr: false,
});

type ClassDetailPageClientProps = {
    classId: number;
};

export default function ClassDetailPageClient({ classId }: ClassDetailPageClientProps) {
    return <ClassDetailClient classId={classId} />;
}
