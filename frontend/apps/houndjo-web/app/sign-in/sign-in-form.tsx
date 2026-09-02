"use client";

import { useState } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2, LogIn } from "lucide-react";
import { signIn } from "next-auth/react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { IconFacebook, IconGithub } from "@/assets/brand-icons";
import { cn } from "@/lib/utils";
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

const formSchema = z.object({
    email: z.email({
        error: (iss) =>
            iss.input === "" ? "Please enter your email" : undefined,
    }),
    password: z
        .string()
        .min(1, "Please enter your password")
        .min(7, "Password must be at least 7 characters long"),
    rememberMe: z.boolean(),
});

type FormValues = z.infer<typeof formSchema>;

type SignInFormProps = React.HTMLAttributes<HTMLFormElement> & {
    redirectTo?: string;
};

function sanitizeRedirect(redirectTo?: string) {
    if (!redirectTo?.startsWith("/") || redirectTo.startsWith("//")) {
        return "/dashboard";
    }

    return redirectTo;
}

function getMfaChallengeId(error?: string | null) {
    if (!error?.startsWith("MFA_REQUIRED:")) {
        return null;
    }

    return error.slice("MFA_REQUIRED:".length);
}

export function SignInForm({
    className,
    redirectTo,
    ...props
}: SignInFormProps) {
    const [isLoading, setIsLoading] = useState(false);
    const router = useRouter();

    const form = useForm<FormValues>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            email: "",
            password: "",
            rememberMe: false,
        },
    });

    async function onSubmit(data: FormValues) {
        setIsLoading(true);
        const callbackUrl = sanitizeRedirect(redirectTo);

        const result = await signIn("credentials", {
            email: data.email,
            password: data.password,
            rememberMe: data.rememberMe ? "true" : "false",
            redirect: false,
            callbackUrl,
        });

        setIsLoading(false);
        const challengeId = getMfaChallengeId(result?.error);
        if (challengeId) {
            router.push(
                `/otp?challengeId=${encodeURIComponent(challengeId)}&redirect=${encodeURIComponent(callbackUrl)}`
            );
            return;
        }

        if (result?.error) {
            toast.error("Invalid email or password");
            return;
        }

        toast.success(`Welcome back, ${data.email}!`);
        router.push(result?.url || callbackUrl);
        router.refresh();
    }

    return (
        <Form {...form}>
            <form
                onSubmit={form.handleSubmit(onSubmit)}
                className={cn("grid gap-3", className)}
                {...props}
            >
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
                        <FormItem className="relative">
                            <FormLabel>Password</FormLabel>
                            <FormControl>
                                <PasswordInput
                                    placeholder="********"
                                    {...field}
                                />
                            </FormControl>
                            <FormMessage />
                            <Link
                                href="/forgot-password"
                                className="absolute end-0 -top-0.5 text-sm font-medium text-muted-foreground hover:opacity-75"
                            >
                                Forgot password?
                            </Link>
                        </FormItem>
                    )}
                />
                <Button className="mt-2" disabled={isLoading}>
                    {isLoading ? (
                        <Loader2 className="animate-spin" />
                    ) : (
                        <LogIn />
                    )}
                    Sign in
                </Button>
                <FormField
                    control={form.control}
                    name="rememberMe"
                    render={({ field }) => (
                        <FormItem>
                            <label className="inline-flex items-center gap-2 text-sm text-muted-foreground">
                                <input
                                    type="checkbox"
                                    checked={field.value}
                                    onChange={field.onChange}
                                    onBlur={field.onBlur}
                                />
                                Remember me
                            </label>
                        </FormItem>
                    )}
                />

                <div className="relative my-2">
                    <div className="absolute inset-0 flex items-center">
                        <span className="w-full border-t" />
                    </div>
                    <div className="relative flex justify-center text-xs uppercase">
                        <span className="bg-background px-2 text-muted-foreground">
                            Or continue with
                        </span>
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-2">
                    <Button
                        variant="outline"
                        type="button"
                        disabled
                        onClick={() =>
                            toast.info("GitHub provider is not configured yet")
                        }
                    >
                        <IconGithub className="h-4 w-4" /> GitHub
                    </Button>
                    <Button
                        variant="outline"
                        type="button"
                        disabled
                        onClick={() =>
                            toast.info(
                                "Facebook provider is not configured yet"
                            )
                        }
                    >
                        <IconFacebook className="h-4 w-4" /> Facebook
                    </Button>
                </div>
            </form>
        </Form>
    );
}
