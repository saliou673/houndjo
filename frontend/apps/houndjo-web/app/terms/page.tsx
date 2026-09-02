import type { Metadata } from "next";
import { Footer } from "@/components/landing/footer";
import { LandingNavbar } from "@/components/landing/navbar";
import { Terms } from "@/features/terms";

export const metadata: Metadata = {
    title: "Terms of Service — Houndjo",
    description: "Read the Houndjo terms of service and usage conditions.",
};

export default function TermsPage() {
    return (
        <>
            <LandingNavbar />
            <main className="pt-16">
                <Terms />
            </main>
            <Footer />
        </>
    );
}
