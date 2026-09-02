import {
    KeyRound,
    Users,
    Settings,
    Globe2,
    Code2,
    TestTube2,
} from "lucide-react";
import {
    Card,
    CardContent,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";

const FEATURES = [
    {
        icon: KeyRound,
        title: "Authentication & account lifecycle",
        description:
            "Email/password sign-up with activation, JWT auth with refresh-token rotation, 2FA (TOTP), password reset, and admin-managed invitations — all built in.",
    },
    {
        icon: Users,
        title: "Role-based access control",
        description:
            "Role groups composed of fine-grained permissions, enforced end to end. Manage role groups and assignments from the admin dashboard.",
    },
    {
        icon: Settings,
        title: "Admin & platform tooling",
        description:
            "User administration, runtime app configuration, security settings, rate limiting, and health checks — the plumbing every SaaS needs, already wired.",
    },
    {
        icon: Globe2,
        title: "Internationalized from day one",
        description:
            "Backend errors, emails, and the admin UI are fully localized (English/French out of the box) with a language switcher that persists per user.",
    },
    {
        icon: Code2,
        title: "Fully typed, end to end",
        description:
            "A TanStack Query client generated from the backend's OpenAPI spec — no hand-written HTTP calls, shared between web and mobile.",
    },
    {
        icon: TestTube2,
        title: "Tested and production-minded",
        description:
            "Hexagonal architecture on the backend, JUnit 5 + Testcontainers integration tests, and Stripe/AWS S3 already wired in for when you need them.",
    },
];

export function Features() {
    return (
        <section id="features" className="bg-muted/40 py-20 sm:py-28">
            <div className="mx-auto max-w-7xl px-4 sm:px-6">
                <div className="mb-14 text-center">
                    <p className="mb-2 text-sm font-semibold tracking-widest text-primary uppercase">
                        Everything included
                    </p>
                    <h2 className="text-3xl font-extrabold tracking-tight text-foreground sm:text-4xl">
                        The boilerplate, already solved
                    </h2>
                    <p className="mx-auto mt-4 max-w-xl text-base text-muted-foreground">
                        Authentication, RBAC, and admin tooling take weeks to
                        build right. Here, they're already done.
                    </p>
                </div>

                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {FEATURES.map((feature) => (
                        <Card key={feature.title} className="gap-3">
                            <CardHeader>
                                <div className="mb-2 flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10">
                                    <feature.icon className="h-5 w-5 text-primary" />
                                </div>
                                <CardTitle className="text-base">
                                    {feature.title}
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-sm leading-relaxed text-muted-foreground">
                                    {feature.description}
                                </p>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            </div>
        </section>
    );
}
