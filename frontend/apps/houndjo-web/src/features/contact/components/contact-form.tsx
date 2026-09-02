"use client";

import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useSendContactForm } from "houndjo-apiclient";
import { Loader2, Send } from "lucide-react";
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
import { Textarea } from "@/components/ui/textarea";

const contactSchema = z.object({
    name: z.string().min(1, "Please enter your name").max(100),
    email: z.email({
        error: (iss) =>
            iss.input === "" ? "Please enter your email" : undefined,
    }),
    subject: z.string().min(1, "Please enter a subject").max(150),
    message: z.string().min(1, "Please enter your message").max(5000),
});

export function ContactForm({
    className,
    ...props
}: React.HTMLAttributes<HTMLFormElement>) {
    const { mutate: sendContactForm, isPending } = useSendContactForm();

    const form = useForm<z.infer<typeof contactSchema>>({
        resolver: zodResolver(contactSchema),
        defaultValues: { name: "", email: "", subject: "", message: "" },
    });

    function onSubmit(data: z.infer<typeof contactSchema>) {
        sendContactForm(
            { data },
            {
                onSuccess: () => {
                    toast.success(
                        "Message sent! We'll get back to you shortly."
                    );
                    form.reset();
                },
                onError: () => {
                    toast.error("Something went wrong. Please try again.");
                },
            }
        );
    }

    return (
        <Form {...form}>
            <form
                onSubmit={form.handleSubmit(onSubmit)}
                className={cn("grid gap-4", className)}
                {...props}
            >
                <div className="grid gap-4 sm:grid-cols-2">
                    <FormField
                        control={form.control}
                        name="name"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>Name</FormLabel>
                                <FormControl>
                                    <Input placeholder="Jane Doe" {...field} />
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
                                        placeholder="jane@example.com"
                                        {...field}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                </div>
                <FormField
                    control={form.control}
                    name="subject"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Subject</FormLabel>
                            <FormControl>
                                <Input
                                    placeholder="How can we help?"
                                    {...field}
                                />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="message"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Message</FormLabel>
                            <FormControl>
                                <Textarea
                                    placeholder="Tell us more about your question or feedback..."
                                    className="min-h-32 resize-none"
                                    {...field}
                                />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <Button
                    className="mt-2 bg-violet-600 text-white shadow-lg shadow-violet-500/25 hover:bg-violet-500 hover:shadow-violet-500/40"
                    disabled={isPending}
                >
                    {isPending ? (
                        <Loader2 className="animate-spin" />
                    ) : (
                        <Send />
                    )}
                    Send Message
                </Button>
            </form>
        </Form>
    );
}
