import type { Metadata } from "next";
import { NotFoundError } from "@/features/errors/not-found-error";

export const metadata: Metadata = {
    title: "Page Not Found",
};

export default function NotFoundPage() {
    return <NotFoundError />;
}
