import Link from "next/link";
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { AuthLayout } from "../auth-layout";
import { CompleteInvitationForm } from "./components/complete-invitation-form";

export function CompleteInvitation() {
    return (
        <AuthLayout>
            <Card className="gap-4">
                <CardHeader>
                    <CardTitle className="text-lg tracking-tight">
                        Activate your account
                    </CardTitle>
                    <CardDescription>
                        You have been invited to join the platform. Set a
                        password to activate your account.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <CompleteInvitationForm />
                </CardContent>
                <CardFooter>
                    <p className="mx-auto px-8 text-center text-sm text-balance text-muted-foreground">
                        Already have an account?{" "}
                        <Link
                            href="/sign-in"
                            className="underline underline-offset-4 hover:text-primary"
                        >
                            Sign in
                        </Link>
                        .
                    </p>
                </CardFooter>
            </Card>
        </AuthLayout>
    );
}
