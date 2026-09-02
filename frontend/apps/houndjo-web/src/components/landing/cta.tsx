import { ArrowRight, Github, Star } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";

const GITHUB_URL = "https://github.com/saliou673/spring-next-saas-starter";

export function CTA() {
    return (
        <section className="bg-background py-20 sm:py-28">
            <div className="mx-auto max-w-7xl px-4 sm:px-6">
                {/* Star on GitHub banner */}
                <div className="mb-6 overflow-hidden rounded-xl border border-border bg-muted">
                    <div className="flex flex-col items-start justify-between gap-4 px-6 py-5 sm:flex-row sm:items-center">
                        <div className="flex items-center gap-4">
                            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-primary/10">
                                <Star className="h-6 w-6 text-primary" />
                            </div>
                            <div>
                                <p className="text-base font-bold text-foreground">
                                    Free and open source
                                </p>
                                <p className="text-sm text-muted-foreground">
                                    MIT licensed — fork it, rename it, ship
                                    your own SaaS on top of it.
                                </p>
                            </div>
                        </div>
                        <Button variant="outline" asChild className="shrink-0">
                            <Link href={GITHUB_URL} target="_blank" rel="noreferrer">
                                <Github className="h-4 w-4" />
                                Star on GitHub
                            </Link>
                        </Button>
                    </div>
                </div>

                {/* Main CTA */}
                <div className="overflow-hidden rounded-xl border border-border bg-card shadow-md">
                    <div className="relative px-8 py-14 sm:px-12 sm:py-20">
                        <div className="pointer-events-none absolute top-0 right-0 h-64 w-64 translate-x-1/3 -translate-y-1/3 rounded-full bg-primary/5 blur-3xl" />
                        <div className="pointer-events-none absolute bottom-0 left-0 h-48 w-48 -translate-x-1/3 translate-y-1/3 rounded-full bg-primary/5 blur-3xl" />

                        <div className="relative z-10 mx-auto max-w-2xl text-center">
                            <p className="mb-3 text-sm font-semibold tracking-widest text-primary uppercase">
                                Ready when you are
                            </p>
                            <h2 className="text-3xl font-extrabold tracking-tight text-foreground sm:text-4xl">
                                Start building your SaaS today
                            </h2>

                            <p className="mx-auto mt-4 max-w-lg text-base text-muted-foreground">
                                Create an account to explore the admin
                                dashboard, or clone the repo and start
                                building on top of it right away.
                            </p>

                            <div className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row">
                                <Button size="lg" asChild>
                                    <Link href="/sign-up">
                                        Create free account
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

                            <p className="mt-5 text-sm text-muted-foreground">
                                No credit card required · MIT licensed
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}
