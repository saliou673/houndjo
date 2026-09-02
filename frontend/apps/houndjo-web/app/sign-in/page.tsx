import type { Metadata } from "next";
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { AuthLayout } from "@/features/auth/auth-layout";
import { SignInForm } from "./sign-in-form";

export const metadata: Metadata = {
    title: "Sign In",
};

type SignInPageProps = {
    searchParams?: Promise<{ redirect?: string }>;
};

export default async function SignInPage({ searchParams }: SignInPageProps) {
    const params = await searchParams;

    return (
        <AuthLayout>
            <Card className="gap-4">
                <CardHeader>
                    <CardTitle className="text-lg tracking-tight">
                        Sign in
                    </CardTitle>
                    <CardDescription>
                        Enter your email and password below to <br />
                        log into your account
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <SignInForm redirectTo={params?.redirect} />
                </CardContent>
                <CardFooter>
                    <p className="px-8 text-center text-sm text-muted-foreground">
                        By clicking sign in, you agree to our{" "}
                        <a
                            href="/terms"
                            className="underline underline-offset-4 hover:text-primary"
                        >
                            Terms of Service
                        </a>{" "}
                        and{" "}
                        <a
                            href="/privacy"
                            className="underline underline-offset-4 hover:text-primary"
                        >
                            Privacy Policy
                        </a>
                        .
                    </p>
                </CardFooter>
            </Card>
        </AuthLayout>
    );
}
