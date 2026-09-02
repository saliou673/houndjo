"use client";

import { useState, useEffect } from "react";
import { Menu, X, Layers, Github } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { ThemeSwitch } from "@/components/theme-switch";

const NAV_LINKS = [
    { label: "Features", href: "#features" },
    { label: "Tech stack", href: "#tech-stack" },
    { label: "How it works", href: "#process" },
];

const GITHUB_URL = "https://github.com/saliou673/spring-next-saas-starter";

export function LandingNavbar() {
    const [scrolled, setScrolled] = useState(false);
    const [mobileOpen, setMobileOpen] = useState(false);

    useEffect(() => {
        const handleScroll = () => setScrolled(window.scrollY > 20);
        window.addEventListener("scroll", handleScroll, { passive: true });
        return () => window.removeEventListener("scroll", handleScroll);
    }, []);

    return (
        <header
            className={`fixed inset-x-0 top-0 z-50 transition-all duration-300 ${
                scrolled
                    ? "border-b border-border bg-background/90 shadow-xs backdrop-blur-xl"
                    : "bg-transparent"
            }`}
        >
            <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
                {/* Logo */}
                <Link href="/" className="group flex items-center gap-2.5">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground transition-transform duration-200 group-hover:scale-105">
                        <Layers className="h-4 w-4" />
                    </div>
                    <span className="text-base font-bold tracking-tight text-foreground">
                        Houndjo
                    </span>
                </Link>

                {/* Desktop nav */}
                <nav className="hidden items-center gap-1 md:flex">
                    {NAV_LINKS.map((link) => (
                        <Link
                            key={link.label}
                            href={link.href}
                            className="rounded-md px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground"
                        >
                            {link.label}
                        </Link>
                    ))}
                </nav>

                {/* Desktop CTA */}
                <div className="hidden items-center gap-2 md:flex">
                    <ThemeSwitch />
                    <Button variant="ghost" size="icon" asChild>
                        <Link
                            href={GITHUB_URL}
                            target="_blank"
                            rel="noreferrer"
                            aria-label="View on GitHub"
                        >
                            <Github className="h-4 w-4" />
                        </Link>
                    </Button>
                    <Button variant="ghost" size="sm" asChild>
                        <Link href="/sign-in">Sign in</Link>
                    </Button>
                    <Button size="sm" asChild>
                        <Link href="/sign-up">Get started</Link>
                    </Button>
                </div>

                {/* Mobile toggle */}
                <button
                    className="rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground md:hidden"
                    onClick={() => setMobileOpen(!mobileOpen)}
                    aria-label="Toggle menu"
                >
                    {mobileOpen ? (
                        <X className="h-5 w-5" />
                    ) : (
                        <Menu className="h-5 w-5" />
                    )}
                </button>
            </div>

            {/* Mobile menu */}
            {mobileOpen && (
                <div className="border-t border-border bg-background/95 backdrop-blur-xl md:hidden">
                    <div className="mx-auto max-w-7xl space-y-1 px-4 py-3">
                        {NAV_LINKS.map((link) => (
                            <Link
                                key={link.label}
                                href={link.href}
                                className="block rounded-md px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground"
                                onClick={() => setMobileOpen(false)}
                            >
                                {link.label}
                            </Link>
                        ))}
                        <Link
                            href={GITHUB_URL}
                            target="_blank"
                            rel="noreferrer"
                            className="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground"
                            onClick={() => setMobileOpen(false)}
                        >
                            <Github className="h-4 w-4" />
                            View on GitHub
                        </Link>
                        <div className="flex items-center justify-between border-t border-border pt-3 pb-1">
                            <span className="px-3 text-xs text-muted-foreground">
                                Theme
                            </span>
                            <ThemeSwitch />
                        </div>
                        <div className="flex flex-col gap-2">
                            <Button variant="ghost" size="sm" asChild className="justify-start">
                                <Link href="/sign-in">Sign in</Link>
                            </Button>
                            <Button size="sm" asChild>
                                <Link href="/sign-up">Get started</Link>
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        </header>
    );
}
