import type { Metadata } from "next";
import { Footer } from "@/components/landing/footer";
import { LandingNavbar } from "@/components/landing/navbar";
import { CookiePolicy } from "@/features/cookie-policy";

export const metadata: Metadata = {
    title: "Cookie Policy — Houndjo",
    description: "Learn how Houndjo uses cookies and similar technologies.",
};

export default function CookiePolicyPage() {
    return (
        <>
            <LandingNavbar />
            <main className="pt-16">
                <CookiePolicy />
            </main>
            <Footer />
        </>
    );
}
