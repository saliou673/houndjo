import type { Metadata } from "next";
import { ForgotPassword } from "@/features/auth/forgot-password";

export const metadata: Metadata = {
    title: "Forgot Password",
};

export default function ForgotPasswordPage() {
    return <ForgotPassword />;
}
