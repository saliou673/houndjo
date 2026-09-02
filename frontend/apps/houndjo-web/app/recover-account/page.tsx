import type { Metadata } from "next";
import { RecoverAccount } from "@/features/auth/recover-account";

export const metadata: Metadata = {
    title: "Recover Account",
};

export default function RecoverAccountPage() {
    return <RecoverAccount />;
}
