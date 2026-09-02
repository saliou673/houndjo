import { ArrowRight, Github, Sparkles, ShieldCheck, Layers, Globe2 } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";

const GITHUB_URL = "https://github.com/saliou673/spring-next-saas-starter";

const QUICKSTART = [
    { cmd: "git clone", args: "git@github.com:saliou673/spring-next-saas-starter.git" },
    { cmd: "docker compose -f docker/docker-compose.yml", args: "up -d" },
    { cmd: "pnpm install", args: "" },
    { cmd: "pnpm generate:api", args: "" },
    { cmd: "pnpm web", args: "" },
];

const TRUST_ITEMS = [
    { icon: ShieldCheck, label: "Auth & RBAC built-in" },
    { icon: Layers, label: "Typed API client, end to end" },
    { icon: Globe2, label: "Internationalized from day one" },
];

export function Hero() {
    return (
        <section className="relative overflow-hidden bg-background pt-32 pb-20 sm:pb-28">
            <div className="pointer-events-none absolute inset-0 [background-image:radial-gradient(circle,var(--color-border)_1px,transparent_1px)] [background-size:32px_32px] opacity-40" />

            <div className="relative z-10 mx-auto grid max-w-7xl gap-12 px-4 sm:px-6 lg:grid-cols-2 lg:items-center">
                {/* Copy */}
                <div>
                    <div className="mb-5 inline-flex items-center gap-2 rounded-full border border-border bg-muted px-3.5 py-1.5 text-sm text-muted-foreground">
                        <Sparkles className="h-3.5 w-3.5" />
                        Open source · MIT licensed
                    </div>
                    <h1 className="max-w-xl text-4xl font-extrabold tracking-tight text-foreground sm:text-5xl lg:text-6xl">
                        Ship your SaaS
                        <span className="block text-primary">
                            faster than ever
                        </span>
                    </h1>
                    <p className="mt-5 max-w-lg text-lg text-muted-foreground">
                        A production-ready Next.js + Spring Boot starter with
                        authentication, RBAC, admin tooling, and a fully typed
                        API client already wired up — so you can focus on your
                        product's actual domain from day one.
                    </p>

                    <div className="mt-8 flex flex-col gap-3 sm:flex-row">
                        <Button size="lg" asChild>
                            <Link href="/sign-up">
                                Get started
                                <ArrowRight className="h-4 w-4" />
                            </Link>
                        </Button>
                        <Button variant="outline" size="lg" asChild>
                            <Link href={GITHUB_URL} target="_blank" rel="noreferrer">
                                <Github className="h-4 w-4" />
                                View on GitHub
                            </Link>
                        </Button>
                    </div>

                    <div className="mt-10 flex flex-wrap items-center gap-x-6 gap-y-3">
                        {TRUST_ITEMS.map((item) => (
                            <div
                                key={item.label}
                                className="flex items-center gap-2 text-sm font-medium text-muted-foreground"
                            >
                                <item.icon className="h-4 w-4 text-primary" />
                                {item.label}
                            </div>
                        ))}
                    </div>
                </div>

                {/* Quickstart terminal card */}
                <div className="overflow-hidden rounded-xl border border-border bg-card shadow-lg">
                    <div className="flex items-center gap-1.5 border-b border-border px-4 py-3">
                        <span className="h-2.5 w-2.5 rounded-full bg-muted-foreground/20" />
                        <span className="h-2.5 w-2.5 rounded-full bg-muted-foreground/20" />
                        <span className="h-2.5 w-2.5 rounded-full bg-muted-foreground/20" />
                        <span className="ms-3 text-xs font-medium text-muted-foreground">
                            quickstart.sh
                        </span>
                    </div>
                    <div className="space-y-3 p-5 font-mono text-sm">
                        {QUICKSTART.map((line, i) => (
                            <div key={i} className="flex flex-wrap gap-x-2">
                                <span className="select-none text-primary">
                                    $
                                </span>
                                <span className="text-foreground">
                                    {line.cmd}
                                </span>
                                {line.args && (
                                    <span className="text-muted-foreground">
                                        {line.args}
                                    </span>
                                )}
                            </div>
                        ))}
                        <div className="flex gap-x-2 pt-1">
                            <span className="select-none text-primary">
                                →
                            </span>
                            <span className="text-muted-foreground">
                                App running on localhost:3000
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}
