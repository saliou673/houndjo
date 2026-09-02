"use client";

import { useState } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useCompleteInvitation } from "@api-client";
import { Loader2 } from "lucide-react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
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
import { PasswordInput } from "@/components/password-input";

const PASSWORD_PATTERN =
    /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$/;

const formSchema = z
    .object({
        newPassword: z
            .string()
            .min(8, "Password must be at least 8 characters")
            .regex(
                PASSWORD_PATTERN,
                "Password must contain uppercase, lowercase, digit and special character"
            ),
        confirmPassword: z.string().min(1, "Please confirm your password"),
    })
    .refine((data) => data.newPassword === data.confirmPassword, {
        message: "Passwords do not match",
        path: ["confirmPassword"],
    });

export function CompleteInvitationForm({
    className,
    ...props
}: React.HTMLAttributes<HTMLFormElement>) {
    const searchParams = useSearchParams();
    const code = searchParams.get("code") ?? "";
    const [completed, setCompleted] = useState(false);

    const { mutate: completeInvitation, isPending } = useCompleteInvitation();

    const form = useForm<z.infer<typeof formSchema>>({
        resolver: zodResolver(formSchema),
        defaultValues: { newPassword: "", confirmPassword: "" },
    });

    if (!code) {
        return (
            <p className="text-sm text-destructive">
                Invalid or missing invitation link. Please contact your
                administrator.
            </p>
        );
    }

    if (completed) {
        return (
            <div className="grid gap-3 text-sm">
                <p className="text-muted-foreground">
                    Your account is ready. You can now sign in with your email
                    and the password you just set.
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

    function onSubmit(data: z.infer<typeof formSchema>) {
        completeInvitation(
            { data: { code, newPassword: data.newPassword } },
            {
                onSuccess: () => setCompleted(true),
                onError: (error) => {
                    const status = error.response?.status;
                    if (status === 404) {
                        toast.error(
                            "Invitation not found or has already been used."
                        );
                    } else if (status === 409) {
                        toast.error(
                            "This invitation has expired or is no longer valid."
                        );
                    } else if (status === 403) {
                        toast.error(
                            "You are not authorized to complete this invitation."
                        );
                    } else {
                        toast.error(
                            "Something went wrong. Please try again later."
                        );
                    }
                },
            }
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
                    name="newPassword"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>New password</FormLabel>
                            <FormControl>
                                <PasswordInput
                                    placeholder="Min. 8 chars, upper, lower, digit, symbol"
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
                            <FormLabel>Confirm password</FormLabel>
                            <FormControl>
                                <PasswordInput
                                    placeholder="Repeat your password"
                                    {...field}
                                />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <Button className="mt-2" disabled={isPending}>
                    {isPending && <Loader2 className="animate-spin" />}
                    Activate account
                </Button>
            </form>
        </Form>
    );
}
