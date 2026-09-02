import type { Metadata } from "next";
import { ForbiddenError } from "@/features/errors/forbidden";

export const metadata: Metadata = {
    title: "Access Forbidden",
};

export default function ForbiddenPage() {
    return <ForbiddenError />;
}
