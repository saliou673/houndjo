import type { Metadata } from "next";
import { Otp } from "@/features/auth/otp";

export const metadata: Metadata = {
    title: "Verify OTP",
};

export default function OtpPage() {
    return <Otp />;
}
