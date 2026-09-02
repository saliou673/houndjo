import type { Metadata } from "next";
import { GeneralError } from "@/features/errors/general-error";

export const metadata: Metadata = {
    title: "Server Error",
};

export default function InternalServerErrorPage() {
    return <GeneralError />;
}
