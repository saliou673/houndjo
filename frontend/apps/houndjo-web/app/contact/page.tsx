import type { Metadata } from "next";
import { Footer } from "@/components/landing/footer";
import { LandingNavbar } from "@/components/landing/navbar";
import { Contact } from "@/features/contact";

export const metadata: Metadata = {
    title: "Contact — Houndjo",
    description: "Get in touch with the Houndjo team.",
};

export default function ContactPage() {
    return (
        <>
            <LandingNavbar />
            <main className="pt-16">
                <Contact />
            </main>
            <Footer />
        </>
    );
}
