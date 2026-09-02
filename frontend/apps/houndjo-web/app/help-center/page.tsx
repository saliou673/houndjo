import type { Metadata } from "next";
import HelpCenterClient from "./help-center-client";

export const metadata: Metadata = {
    title: "Help Center",
};

export default function HelpCenterPage() {
    return <HelpCenterClient />;
}
