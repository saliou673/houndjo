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
import { RecoverAccountForm } from "./components/recover-account-form";

export function RecoverAccount() {
    return (
        <AuthLayout>
            <Card className="gap-4">
                <CardHeader>
                    <CardTitle className="text-lg tracking-tight">
                        Recover your account
                    </CardTitle>
                    <CardDescription>
                        Enter your email and password to recover your
                        deactivated account. Your data will remain intact if
                        recovered within 30 days.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <RecoverAccountForm />
                </CardContent>
                <CardFooter>
                    <p className="mx-auto px-8 text-center text-sm text-balance text-muted-foreground">
                        Remember your password?{" "}
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
