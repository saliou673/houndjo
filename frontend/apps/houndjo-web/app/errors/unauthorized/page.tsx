import type { Metadata } from "next";
import { UnauthorisedError } from "@/features/errors/unauthorized-error";

export const metadata: Metadata = {
    title: "Unauthorized",
};

export default function UnauthorizedPage() {
    return <UnauthorisedError />;
}
