import { Layers, Github } from "lucide-react";
import Link from "next/link";

const GITHUB_URL = "https://github.com/saliou673/spring-next-saas-starter";

const FOOTER_LINKS = {
    Product: [
        { label: "Features", href: "#features" },
        { label: "Tech stack", href: "#tech-stack" },
        { label: "How it works", href: "#process" },
        { label: "Sign in", href: "/sign-in" },
    ],
    Resources: [
        { label: "GitHub repository", href: GITHUB_URL, external: true },
        { label: "Help center", href: "/help-center" },
        { label: "Contact us", href: "/contact" },
    ],
    Legal: [
        { label: "Privacy", href: "/privacy" },
        { label: "Terms", href: "/terms" },
        { label: "Cookie policy", href: "/cookie-policy" },
    ],
};

export function Footer() {
    return (
        <footer className="bg-background">
            <div className="border-t border-border" />

            <div className="mx-auto max-w-7xl px-4 pt-14 pb-10 sm:px-6">
                <div className="grid gap-10 sm:grid-cols-2 md:grid-cols-4">
                    {/* Brand column */}
                    <div className="sm:col-span-2 md:col-span-1">
                        <Link href="/" className="mb-4 inline-flex items-center gap-2.5">
                            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                                <Layers className="h-4 w-4" />
                            </div>
                            <span className="text-base font-bold text-foreground">
                                Houndjo
                            </span>
                        </Link>
                        <p className="mb-5 max-w-xs text-sm leading-relaxed text-muted-foreground">
                            A production-ready Next.js + Spring Boot starter
                            with auth, RBAC, and admin tooling built in.
                        </p>
                        <Link
                            href={GITHUB_URL}
                            target="_blank"
                            rel="noreferrer"
                            className="inline-flex text-muted-foreground transition-colors hover:text-foreground"
                            aria-label="GitHub"
                        >
                            <Github className="h-5 w-5" />
                        </Link>
                    </div>

                    {/* Link columns */}
                    {Object.entries(FOOTER_LINKS).map(([category, links]) => (
                        <div key={category}>
                            <h4 className="mb-4 text-[11px] font-bold tracking-widest text-muted-foreground uppercase">
                                {category}
                            </h4>
                            <ul className="space-y-3">
                                {links.map((link) => (
                                    <li key={link.label}>
                                        <Link
                                            href={link.href}
                                            target={
                                                "external" in link && link.external
                                                    ? "_blank"
                                                    : undefined
                                            }
                                            rel={
                                                "external" in link && link.external
                                                    ? "noreferrer"
                                                    : undefined
                                            }
                                            className="text-sm text-muted-foreground transition-colors hover:text-foreground"
                                        >
                                            {link.label}
                                        </Link>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    ))}
                </div>

                <div className="mt-12 flex flex-col items-center justify-between gap-3 border-t border-border pt-8 sm:flex-row">
                    <p className="text-sm text-muted-foreground">
                        © {new Date().getFullYear()} Houndjo. MIT licensed.
                    </p>
                    <p className="text-xs text-muted-foreground">
                        Built with Next.js &amp; Spring Boot
                    </p>
                </div>
            </div>
        </footer>
    );
}
