"use client";

import { useRef, useState } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
    useRequestPasswordReset,
    useFinishPasswordReset,
} from "houndjo-apiclient";
import { ArrowRight, Loader2 } from "lucide-react";
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
import {
    InputOTP,
    InputOTPGroup,
    InputOTPSlot,
} from "@/components/ui/input-otp";
import { PasswordInput } from "@/components/password-input";

const emailSchema = z.object({
    email: z.email({
        error: (iss) =>
            iss.input === "" ? "Please enter your email" : undefined,
    }),
});

const resetSchema = z
    .object({
        newPassword: z
            .string()
            .regex(
                /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$/,
                "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."
            ),
        confirmPassword: z.string(),
    })
    .refine((data) => data.newPassword === data.confirmPassword, {
        message: "Passwords do not match",
        path: ["confirmPassword"],
    });

export function ForgotPasswordForm({
    className,
    ...props
}: React.HTMLAttributes<HTMLFormElement>) {
    const [step, setStep] = useState<"email" | "reset" | "done">("email");
    const [code, setCode] = useState("");
    const newPasswordRef = useRef<HTMLInputElement>(null);

    const { mutate: requestPasswordReset, isPending: isRequestPending } =
        useRequestPasswordReset();
    const { mutate: finishPasswordReset, isPending: isFinishPending } =
        useFinishPasswordReset();

    const emailForm = useForm<z.infer<typeof emailSchema>>({
        resolver: zodResolver(emailSchema),
        defaultValues: { email: "" },
    });

    const resetForm = useForm<z.infer<typeof resetSchema>>({
        resolver: zodResolver(resetSchema),
        defaultValues: { newPassword: "", confirmPassword: "" },
    });

    function onEmailSubmit(data: z.infer<typeof emailSchema>) {
        requestPasswordReset(
            { data: data.email },
            {
                onSuccess: () => {
                    toast.success(`Reset code sent to ${data.email}`);
                    setStep("reset");
                },
                onError: (error) => {
                    const status = error.response?.status;
                    if (status === 404) {
                        toast.error(
                            "No account found with this email address."
                        );
                    } else if (status === 409) {
                        const message =
                            (
                                error.response?.data as
                                    | { message?: string }
                                    | undefined
                            )?.message ?? "Unable to send reset code.";
                        toast.error(message);
                    } else {
                        toast.error("Something went wrong. Please try again.");
                    }
                },
            }
        );
    }

    function onResetSubmit(data: z.infer<typeof resetSchema>) {
        if (code.length < 4) return;
        finishPasswordReset(
            { data: { code, newPassword: data.newPassword } },
            {
                onSuccess: () => {
                    setStep("done");
                },
                onError: (error) => {
                    const status = error.response?.status;
                    if (status === 404) {
                        toast.error("No account found.");
                    } else if (status === 403 || status === 409) {
                        const message =
                            (
                                error.response?.data as
                                    | { message?: string }
                                    | undefined
                            )?.message ?? "Invalid or expired reset code.";
                        toast.error(message);
                    } else {
                        toast.error("Something went wrong.");
                    }
                },
            }
        );
    }

    if (step === "done") {
        return (
            <div className="grid gap-3 text-sm">
                <p className="text-muted-foreground">
                    Your password has been reset successfully. You can now sign
                    in with your new password.
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

    if (step === "reset") {
        return (
            <Form {...resetForm}>
                <form
                    onSubmit={resetForm.handleSubmit(onResetSubmit)}
                    className={cn("grid gap-3", className)}
                    {...props}
                >
                    <FormItem>
                        <FormLabel>Verification Code</FormLabel>
                        <InputOTP
                            maxLength={4}
                            inputMode="text"
                            autoFocus
                            value={code}
                            onChange={setCode}
                            onComplete={() => newPasswordRef.current?.focus()}
                            containerClassName="w-full [&>[data-slot=input-otp-group]]:w-full [&>[data-slot=input-otp-group]>div]:flex-1"
                        >
                            <InputOTPGroup>
                                <InputOTPSlot index={0} />
                                <InputOTPSlot index={1} />
                                <InputOTPSlot index={2} />
                                <InputOTPSlot index={3} />
                            </InputOTPGroup>
                        </InputOTP>
                    </FormItem>
                    <FormField
                        control={resetForm.control}
                        name="newPassword"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>New Password</FormLabel>
                                <FormControl>
                                    <PasswordInput
                                        placeholder="********"
                                        {...field}
                                        ref={newPasswordRef}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                    <FormField
                        control={resetForm.control}
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
                    <Button
                        className="mt-2"
                        disabled={code.length < 4 || isFinishPending}
                    >
                        {isFinishPending && (
                            <Loader2 className="animate-spin" />
                        )}
                        Reset Password
                    </Button>
                </form>
            </Form>
        );
    }

    return (
        <Form {...emailForm}>
            <form
                onSubmit={emailForm.handleSubmit(onEmailSubmit)}
                className={cn("grid gap-2", className)}
                {...props}
            >
                <FormField
                    control={emailForm.control}
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
                <Button className="mt-2" disabled={isRequestPending}>
                    Continue
                    {isRequestPending ? (
                        <Loader2 className="animate-spin" />
                    ) : (
                        <ArrowRight />
                    )}
                </Button>
            </form>
        </Form>
    );
}
