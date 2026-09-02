import type { Metadata } from "next";
import { Logo } from "@/assets/logo";
import { SignUpForm } from "./sign-up-form";

export const metadata: Metadata = {
    title: "Sign Up",
};

export default function SignUpPage() {
    return (
        <div className="container grid min-h-svh max-w-none items-center justify-center">
            <div className="mx-auto flex w-full max-w-5xl flex-col items-center gap-8 px-4 py-10">
                {/* Logo */}
                <div className="flex items-center gap-2">
                    <Logo />
                    <span className="text-xl font-medium">Shadcn Admin</span>
                </div>

                <SignUpForm />
            </div>
        </div>
    );
}
