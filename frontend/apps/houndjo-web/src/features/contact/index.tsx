import { Mail, MessageSquare } from "lucide-react";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { ContactForm } from "./components/contact-form";

export function Contact() {
    return (
        <div className="mx-auto max-w-6xl px-4 py-24 sm:px-6">
            <div className="grid gap-12 lg:grid-cols-2 lg:gap-16">
                {/* Left column */}
                <div className="flex flex-col justify-center gap-8">
                    <div>
                        <h1 className="text-4xl font-bold tracking-tight text-gray-900 sm:text-5xl dark:text-white">
                            Get in touch
                        </h1>
                        <p className="mt-4 text-lg text-gray-600 dark:text-zinc-400">
                            Have a question, feedback, or need support? We'd
                            love to hear from you. Send us a message and we'll
                            get back to you as soon as possible.
                        </p>
                    </div>
                    <div className="flex flex-col gap-4">
                        <div className="flex items-start gap-3">
                            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-violet-100 dark:bg-violet-900/30">
                                <Mail className="h-5 w-5 text-violet-600 dark:text-violet-400" />
                            </div>
                            <div>
                                <p className="font-medium text-gray-900 dark:text-white">
                                    Email support
                                </p>
                                <p className="text-sm text-gray-500 dark:text-zinc-400">
                                    We reply within 24 hours on business days.
                                </p>
                            </div>
                        </div>
                        <div className="flex items-start gap-3">
                            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-violet-100 dark:bg-violet-900/30">
                                <MessageSquare className="h-5 w-5 text-violet-600 dark:text-violet-400" />
                            </div>
                            <div>
                                <p className="font-medium text-gray-900 dark:text-white">
                                    Feedback welcome
                                </p>
                                <p className="text-sm text-gray-500 dark:text-zinc-400">
                                    Feature requests, bug reports, or general
                                    thoughts — we read everything.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Right column — form */}
                <Card className="shadow-xl">
                    <CardHeader>
                        <CardTitle>Send a message</CardTitle>
                        <CardDescription>
                            Fill in the form and we'll be in touch shortly.
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <ContactForm />
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
