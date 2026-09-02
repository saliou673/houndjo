import { getToken } from "next-auth/jwt";
import { NextResponse, type NextRequest } from "next/server";

const PUBLIC_PATHS = new Set([
    "/",
    "/contact",
    "/sign-in",
    "/sign-up",
    "/sign-in-2",
    "/forgot-password",
    "/otp",
    "/recover-account",
    "/account/invitation",
    "/errors/unauthorized",
    "/privacy",
    "/terms",
    "/cookie-policy",
]);

const authSecret =
    process.env.AUTH_SECRET ??
    process.env.NEXTAUTH_SECRET ??
    "dev-only-secret-change-me";

function isPublicPath(pathname: string): boolean {
    return PUBLIC_PATHS.has(pathname);
}

function isAuthenticatedToken(
    token: {
        error?: string;
        accessTokenExpires?: number;
    } | null
) {
    if (!token) {
        return false;
    }

    if (token.error === "RefreshAccessTokenError") {
        return false;
    }

    if (
        typeof token.accessTokenExpires === "number" &&
        Date.now() >= token.accessTokenExpires
    ) {
        return false;
    }

    return true;
}

export async function proxy(request: NextRequest) {
    const { pathname, search } = request.nextUrl;
    const sessionToken = await getToken({
        req: request,
        secret: authSecret,
    });
    const isAuthenticated = isAuthenticatedToken(sessionToken);
    const isPublic = isPublicPath(pathname);

    if (
        isPublic &&
        isAuthenticated &&
        (pathname === "/sign-in" || pathname === "/sign-up")
    ) {
        return NextResponse.redirect(new URL("/dashboard", request.url));
    }

    if (!isPublic && !isAuthenticated) {
        const signInUrl = new URL("/sign-in", request.url);
        const redirectTo = `${pathname}${search}`;
        signInUrl.searchParams.set("redirect", redirectTo);
        return NextResponse.redirect(signInUrl);
    }

    return NextResponse.next();
}

export const config = {
    matcher: ["/((?!api|_next/static|_next/image|favicon.ico|.*\\..*).*)"],
};
