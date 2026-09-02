import type { Metadata } from "next";
import { CTA } from "@/components/landing/cta";
import { Features } from "@/components/landing/features";
import { Footer } from "@/components/landing/footer";
import { Hero } from "@/components/landing/hero";
import { LandingNavbar } from "@/components/landing/navbar";
import { Process } from "@/components/landing/process";
import { TechStack } from "@/components/landing/tech-stack";

export const metadata: Metadata = {
    title: "Houndjo — Ship your SaaS faster than ever",
    description:
        "A production-ready Next.js + Spring Boot boilerplate with authentication, roles, billing, multi-tenancy, and more.",
};

export default function HomePage() {
    return (
        <>
            <LandingNavbar />
            <main>
                <Hero />
                <TechStack />
                <Features />
                <Process />
                <CTA />
            </main>
            <Footer />
        </>
    );
}
