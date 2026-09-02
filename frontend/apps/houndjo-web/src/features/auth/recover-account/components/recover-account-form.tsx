"use client";

import { useState } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRecoverAccount } from "houndjo-apiclient";
import { Loader2 } from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";
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
    password: z.string().min(1, "Please enter your password"),
});

export function RecoverAccountForm({
    className,
    ...props
}: React.HTMLAttributes<HTMLFormElement>) {
    const [recovered, setRecovered] = useState(false);
    const { mutate: recoverAccount, isPending } = useRecoverAccount();

    const form = useForm<z.infer<typeof formSchema>>({
        resolver: zodResolver(formSchema),
        defaultValues: { email: "", password: "" },
    });

    function onSubmit(data: z.infer<typeof formSchema>) {
        recoverAccount(
            { data: { email: data.email, password: data.password } },
            {
                onSuccess: () => {
                    setRecovered(true);
                },
                onError: (error) => {
                    const status = error.response?.status;
                    if (status === 404) {
                        toast.error(
                            "No account found with this email address."
                        );
                    } else if (status === 403 || status === 409) {
                        const message =
                            (
                                error.response?.data as
                                    | { message?: string }
                                    | undefined
                            )?.message ??
                            "Account recovery failed. The account may not be deactivated or the recovery period may have expired.";
                        toast.error(message);
                    } else {
                        toast.error("Invalid password.");
                    }
                },
            }
        );
    }

    if (recovered) {
        return (
            <div className="grid gap-3 text-sm">
                <p className="text-muted-foreground">
                    Your account has been successfully recovered. You can now
                    sign in.
                </p>
                <Link
                    href="/sign-in"
                    className="underline underline-offset-4 hover:text-primary"
                >
                    Sign in
                </Link>
            </div>
        );
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
                <Button className="mt-2" disabled={isPending}>
                    {isPending && <Loader2 className="animate-spin" />}
                    Recover account
                </Button>
            </form>
        </Form>
    );
}
