const TECHNOLOGIES = [
    { name: "Next.js", abbr: "N" },
    { name: "React", abbr: "R" },
    { name: "TypeScript", abbr: "TS" },
    { name: "Spring Boot", abbr: "SB" },
    { name: "PostgreSQL", abbr: "PG" },
    { name: "Tailwind CSS", abbr: "TW" },
    { name: "shadcn/ui", abbr: "UI" },
    { name: "TanStack Query", abbr: "TQ" },
    { name: "Kubb", abbr: "KB" },
    { name: "Testcontainers", abbr: "TC" },
];

export function TechStack() {
    return (
        <section
            id="tech-stack"
            className="border-b border-border bg-background py-10"
        >
            <div className="mx-auto max-w-7xl px-4 sm:px-6">
                <p className="mb-6 text-center text-xs font-semibold tracking-widest text-muted-foreground uppercase">
                    Built on a battle-tested stack
                </p>

                <div className="relative overflow-hidden">
                    <div className="pointer-events-none absolute inset-y-0 left-0 z-10 w-20 bg-gradient-to-r from-background to-transparent" />
                    <div className="pointer-events-none absolute inset-y-0 right-0 z-10 w-20 bg-gradient-to-l from-background to-transparent" />

                    <div className="flex gap-0 overflow-hidden">
                        <div className="flex min-w-full shrink-0 animate-marquee items-center gap-8 pr-8">
                            {TECHNOLOGIES.map((t) => (
                                <div
                                    key={t.name}
                                    className="flex shrink-0 items-center gap-2 opacity-70 transition-opacity hover:opacity-100"
                                >
                                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-muted text-[10px] font-extrabold text-foreground">
                                        {t.abbr}
                                    </div>
                                    <span className="text-sm font-semibold text-foreground">
                                        {t.name}
                                    </span>
                                </div>
                            ))}
                        </div>
                        <div
                            aria-hidden="true"
                            className="flex min-w-full shrink-0 animate-marquee items-center gap-8 pr-8"
                        >
                            {TECHNOLOGIES.map((t) => (
                                <div
                                    key={t.name}
                                    className="flex shrink-0 items-center gap-2 opacity-70"
                                >
                                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-muted text-[10px] font-extrabold text-foreground">
                                        {t.abbr}
                                    </div>
                                    <span className="text-sm font-semibold text-foreground">
                                        {t.name}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}
