import {
    GitBranch,
    RefreshCw,
    Rocket,
    Boxes,
    ShieldCheck,
    Gauge,
    Languages,
    CreditCard,
} from "lucide-react";

const HOW_IT_WORKS = [
    {
        step: 1,
        icon: GitBranch,
        title: "Clone & configure",
        description:
            "Clone the repo, start PostgreSQL and MailDev with Docker Compose, and fill in the backend .env file.",
    },
    {
        step: 2,
        icon: RefreshCw,
        title: "Generate the API client",
        description:
            "Run the backend, then pnpm generate:api to produce a fully typed TanStack Query client from the OpenAPI spec.",
    },
    {
        step: 3,
        icon: Rocket,
        title: "Build your product",
        description:
            "Auth, RBAC, and admin tooling are already wired. Start building your actual domain on top, right away.",
    },
];

const WHY_US = [
    {
        icon: Boxes,
        title: "Hexagonal architecture",
        description:
            "The backend follows ports & adapters — domain code stays framework-free and testable in isolation.",
    },
    {
        icon: ShieldCheck,
        title: "Security by default",
        description:
            "JWT refresh-token rotation, TOTP 2FA, and Resilience4j rate limiting on auth and API endpoints out of the box.",
    },
    {
        icon: Gauge,
        title: "Observability included",
        description:
            "Spring Actuator health checks and Prometheus metrics via Micrometer, ready for your monitoring stack.",
    },
    {
        icon: Languages,
        title: "i18n across the stack",
        description:
            "Localized domain errors, emails, and UI — English and French shipped, more locales are a message file away.",
    },
    {
        icon: CreditCard,
        title: "Payments wired in",
        description:
            "Stripe SDK and a payment-mode configuration category are already in place for when you're ready to charge.",
    },
    {
        icon: Rocket,
        title: "Mobile-ready",
        description:
            "An Expo/React Native scaffold consumes the same shared, typed API client as the web app.",
    },
];

export function Process() {
    return (
        <>
            {/* How it works */}
            <section id="process" className="bg-background py-20 sm:py-28">
                <div className="mx-auto max-w-7xl px-4 sm:px-6">
                    <div className="mb-14 text-center">
                        <p className="mb-2 text-sm font-semibold tracking-widest text-primary uppercase">
                            Simple as 1-2-3
                        </p>
                        <h2 className="text-3xl font-extrabold tracking-tight text-foreground sm:text-4xl">
                            How it works
                        </h2>
                        <p className="mx-auto mt-4 max-w-xl text-base text-muted-foreground">
                            Go from a fresh clone to a running dashboard in
                            minutes.
                        </p>
                    </div>

                    <div className="grid gap-6 md:grid-cols-3">
                        {HOW_IT_WORKS.map((item) => {
                            const Icon = item.icon;
                            return (
                                <div
                                    key={item.step}
                                    className="relative overflow-hidden rounded-xl border border-border bg-card transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md"
                                >
                                    <div className="absolute inset-y-0 left-0 w-1 bg-primary" />
                                    <div className="p-6 pl-7">
                                        <div className="mb-5 flex items-start justify-between">
                                            <div className="inline-flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10">
                                                <Icon className="h-5 w-5 text-primary" />
                                            </div>
                                            <span className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-sm font-extrabold text-primary-foreground">
                                                {item.step}
                                            </span>
                                        </div>
                                        <h3 className="mb-2 text-lg font-bold text-foreground">
                                            {item.title}
                                        </h3>
                                        <p className="text-sm leading-relaxed text-muted-foreground">
                                            {item.description}
                                        </p>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </section>

            {/* Why choose us */}
            <section className="bg-muted/40 py-20 sm:py-28">
                <div className="mx-auto max-w-7xl px-4 sm:px-6">
                    <div className="mb-12 text-center">
                        <p className="mb-2 text-sm font-semibold tracking-widest text-primary uppercase">
                            Why start here
                        </p>
                        <h2 className="text-3xl font-extrabold tracking-tight text-foreground sm:text-4xl">
                            Built for real products
                        </h2>
                    </div>

                    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                        {WHY_US.map((item) => {
                            const Icon = item.icon;
                            return (
                                <div
                                    key={item.title}
                                    className="flex gap-4 rounded-xl border border-border bg-card p-5"
                                >
                                    <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-primary/10">
                                        <Icon className="h-5 w-5 text-primary" />
                                    </div>
                                    <div>
                                        <h3 className="mb-1 text-sm font-bold text-foreground">
                                            {item.title}
                                        </h3>
                                        <p className="text-sm leading-relaxed text-muted-foreground">
                                            {item.description}
                                        </p>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </section>
        </>
    );
}
