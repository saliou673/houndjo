"use client";

import { useState } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useCreatePublicUserAccount } from "@api-client";
import { Loader2 } from "lucide-react";
import { signIn, getSession } from "next-auth/react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { IconFacebook, IconGithub } from "@/assets/brand-icons";
import { setApiAccessToken } from "@/lib/apiclient-interceptors";
import { Button } from "@/components/ui/button";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { PasswordInput } from "@/components/password-input";

function NarrowCard({ children }: { children: React.ReactNode }) {
    return (
        <div className="mx-auto w-full max-w-sm rounded-xl border bg-card p-6 shadow-sm">
            {children}
        </div>
    );
}

const formSchema = z
    .object({
        firstName: z.string().min(1, "Please enter your first name"),
        lastName: z.string().min(1, "Please enter your last name"),
        email: z.email({
            error: (iss) =>
                iss.input === "" ? "Please enter your email" : undefined,
        }),
        password: z
            .string()
            .min(1, "Please enter your password")
            .min(8, "Password must be at least 8 characters long")
            .regex(
                /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$/,
                "Password must include upper, lower, number, and special character"
            ),
        confirmPassword: z.string().min(1, "Please confirm your password"),
    })
    .refine((data) => data.password === data.confirmPassword, {
        message: "Passwords don't match.",
        path: ["confirmPassword"],
    });

type FormValues = z.infer<typeof formSchema>;

export function SignUpForm() {
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const createPublicUserAccount = useCreatePublicUserAccount();

    const form = useForm<FormValues>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            firstName: "",
            lastName: "",
            email: "",
            password: "",
            confirmPassword: "",
        },
    });

    async function onSubmit(data: FormValues) {
        setIsLoading(true);
        setError(null);

        // Track separately so the catch block knows what stage we failed at.
        let accountCreated = false;

        try {
            await createPublicUserAccount.mutateAsync({
                data: {
                    firstName: data.firstName,
                    lastName: data.lastName,
                    email: data.email,
                    password: data.password,
                },
            });
            accountCreated = true;

            const result = await signIn("credentials", {
                email: data.email,
                password: data.password,
                redirect: false,
            });

            if (result?.error) {
                // Account was created but auto-sign-in failed (e.g. email
                // verification required). Send to sign-in page.
                toast.success("Account created! Please sign in to continue.");
                router.push("/sign-in");
                return;
            }

            // Prime the API client token so the app can call authenticated
            // endpoints immediately on the next page.
            const session = await getSession();
            setApiAccessToken(session?.accessToken);

            toast.success(`Welcome, ${data.firstName}!`);
            router.push("/dashboard");
            router.refresh();
        } catch (err) {
            if (accountCreated) {
                // Registration succeeded but sign-in threw — redirect to
                // sign-in so the user isn't stuck.
                toast.success("Account created! Please sign in to continue.");
                router.push("/sign-in");
            } else {
                // Registration itself failed — show the server message if any.
                const serverMsg =
                    (
                        err as {
                            response?: { data?: { message?: string } };
                        }
                    )?.response?.data?.message ?? null;
                setError(
                    serverMsg ?? "Unable to create account. Please try again."
                );
            }
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <NarrowCard>
            <div className="grid gap-5">
                <div>
                    <h2 className="text-lg font-semibold tracking-tight">
                        Create an account
                    </h2>
                    <p className="mt-1 text-sm text-muted-foreground">
                        Already have an account?{" "}
                        <Link
                            href="/sign-in"
                            className="underline underline-offset-4 hover:text-primary"
                        >
                            Sign In
                        </Link>
                    </p>
                </div>

                <Form {...form}>
                    <form
                        onSubmit={form.handleSubmit(onSubmit)}
                        className="grid gap-3"
                    >
                        <FormField
                            control={form.control}
                            name="firstName"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>First Name</FormLabel>
                                    <FormControl>
                                        <Input placeholder="John" {...field} />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="lastName"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Last Name</FormLabel>
                                    <FormControl>
                                        <Input placeholder="Doe" {...field} />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="email"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Email</FormLabel>
                                    <FormControl>
                                        <Input
                                            placeholder="name@example.com"
                                            {...field}
                                        />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="password"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Password</FormLabel>
                                    <FormControl>
                                        <PasswordInput
                                            placeholder="********"
                                            {...field}
                                        />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="confirmPassword"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Confirm Password</FormLabel>
                                    <FormControl>
                                        <PasswordInput
                                            placeholder="********"
                                            {...field}
                                        />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />

                        {error && (
                            <p className="text-sm text-destructive">
                                {error}
                            </p>
                        )}

                        <Button className="mt-1" disabled={isLoading}>
                            {isLoading && (
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            )}
                            Create Account
                        </Button>

                        <div className="relative my-1">
                            <div className="absolute inset-0 flex items-center">
                                <span className="w-full border-t" />
                            </div>
                            <div className="relative flex justify-center text-xs uppercase">
                                <span className="bg-card px-2 text-muted-foreground">
                                    Or continue with
                                </span>
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-2">
                            <Button
                                variant="outline"
                                className="w-full"
                                type="button"
                                disabled
                            >
                                <IconGithub className="h-4 w-4" /> GitHub
                            </Button>
                            <Button
                                variant="outline"
                                className="w-full"
                                type="button"
                                disabled
                            >
                                <IconFacebook className="h-4 w-4" /> Facebook
                            </Button>
                        </div>
                    </form>
                </Form>

                <p className="text-center text-xs text-muted-foreground">
                    By creating an account, you agree to our{" "}
                    <a
                        href="/terms"
                        className="underline underline-offset-4 hover:text-primary"
                    >
                        Terms of Service
                    </a>{" "}
                    and{" "}
                    <a
                        href="/privacy"
                        className="underline underline-offset-4 hover:text-primary"
                    >
                        Privacy Policy
                    </a>
                    .
                </p>
            </div>
        </NarrowCard>
    );
}
