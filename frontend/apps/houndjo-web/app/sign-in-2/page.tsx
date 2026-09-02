import type { Metadata } from "next";
import { SignIn2 } from "@/features/auth/sign-in/sign-in-2";

export const metadata: Metadata = {
    title: "Sign In",
};

export default function SignIn2Page() {
    return <SignIn2 />;
}
