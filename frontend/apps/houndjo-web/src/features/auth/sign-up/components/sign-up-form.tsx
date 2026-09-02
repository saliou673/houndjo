import { useState } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useCreatePublicUserAccount } from "@api-client";
import { signIn } from "next-auth/react";
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

export function SignUpForm({
    className,
    ...props
}: React.HTMLAttributes<HTMLFormElement>) {
    const [isLoading, setIsLoading] = useState(false);
    const router = useRouter();
    const createPublicUserAccount = useCreatePublicUserAccount();

    const form = useForm<z.infer<typeof formSchema>>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            firstName: "",
            lastName: "",
            email: "",
            password: "",
            confirmPassword: "",
        },
    });

    async function onSubmit(data: z.infer<typeof formSchema>) {
        setIsLoading(true);

        try {
            await createPublicUserAccount.mutateAsync({
                data: {
                    firstName: data.firstName,
                    lastName: data.lastName,
                    email: data.email,
                    password: data.password,
                },
            });

            const result = await signIn("credentials", {
                email: data.email,
                password: data.password,
                redirect: false,
                callbackUrl: "/dashboard",
            });

            if (result?.error) {
                toast.success("Account created successfully");
                router.push("/sign-in");
                router.refresh();
                return;
            }

            toast.success("Account created successfully");
            router.push(result?.url || "/dashboard");
            router.refresh();
        } catch {
            toast.error("Unable to create account");
        } finally {
            setIsLoading(false);
        }
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
                <Button className="mt-2" disabled={isLoading}>
                    Create Account
                </Button>

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
                        className="w-full"
                        type="button"
                        disabled={isLoading}
                    >
                        <IconGithub className="h-4 w-4" /> GitHub
                    </Button>
                    <Button
                        variant="outline"
                        className="w-full"
                        type="button"
                        disabled={isLoading}
                    >
                        <IconFacebook className="h-4 w-4" /> Facebook
                    </Button>
                </div>
            </form>
        </Form>
    );
}
