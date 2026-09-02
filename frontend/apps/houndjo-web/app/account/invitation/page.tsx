import type { Metadata } from "next";
import { CompleteInvitation } from "@/features/auth/complete-invitation";

export const metadata: Metadata = {
    title: "Accept Invitation",
};

export default function InvitationPage() {
    return <CompleteInvitation />;
}
