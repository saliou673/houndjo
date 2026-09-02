import {
    authenticate,
    refreshToken,
    type JwtToken,
    verifyLoginChallenge,
} from "@api-client";
import NextAuth, { type NextAuthOptions } from "next-auth";
import type { JWT } from "next-auth/jwt";
import Credentials from "next-auth/providers/credentials";

type LoginResponse = JwtToken & {
    challengeId?: string;
};

const apiBaseUrl =
    process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

const authSecret =
    process.env.AUTH_SECRET ??
    process.env.NEXTAUTH_SECRET ??
    (process.env.NODE_ENV === "development"
        ? "dev-only-secret-change-me"
        : undefined);

function decodeJwtExpiration(accessToken: string): number {
    try {
        const payload = JSON.parse(
            Buffer.from(accessToken.split(".")[1], "base64").toString("utf8")
        ) as { exp?: number };

        if (!payload.exp) {
            return Date.now() + 5 * 60 * 1000;
        }

        return payload.exp * 1000;
    } catch {
        return Date.now() + 5 * 60 * 1000;
    }
}

function extractChallengeId(error: unknown): string | undefined {
    if (
        typeof error === "object" &&
        error !== null &&
        "response" in error &&
        typeof error.response === "object" &&
        error.response !== null &&
        "data" in error.response &&
        typeof error.response.data === "object" &&
        error.response.data !== null &&
        "challengeId" in error.response.data &&
        typeof error.response.data.challengeId === "string"
    ) {
        return error.response.data.challengeId;
    }

    return undefined;
}

function extractErrorMessage(error: unknown): string | undefined {
    if (error instanceof Error) {
        return error.message;
    }

    return undefined;
}

async function refreshAccessToken(token: JWT): Promise<JWT> {
    if (!token.refreshToken) {
        return { ...token, error: "RefreshAccessTokenError" };
    }

    try {
        const refreshed = await refreshToken(
            token.refreshToken,
            undefined,
            { baseURL: apiBaseUrl }
        );

        if (!refreshed.accessToken) {
            throw new Error("Refresh response did not include access token");
        }

        return {
            ...token,
            accessToken: refreshed.accessToken,
            refreshToken: refreshed.refreshToken ?? token.refreshToken,
            accessTokenExpires: decodeJwtExpiration(refreshed.accessToken),
            error: undefined,
        };
    } catch {
        return { ...token, error: "RefreshAccessTokenError" };
    }
}

export const authOptions: NextAuthOptions = {
    secret: authSecret,
    session: { strategy: "jwt" },
    pages: {
        signIn: "/sign-in",
    },
    providers: [
        Credentials({
            name: "Credentials",
            credentials: {
                mode: { label: "Mode", type: "text" },
                email: { label: "Email", type: "email" },
                password: { label: "Password", type: "password" },
                rememberMe: { label: "Remember me", type: "text" },
                challengeId: { label: "Challenge ID", type: "text" },
                code: { label: "Code", type: "text" },
            },
            async authorize(credentials) {
                const mode = String(credentials?.mode ?? "password");

                if (mode === "otp") {
                    const challengeId = String(
                        credentials?.challengeId ?? ""
                    ).trim();
                    const code = String(credentials?.code ?? "").trim();

                    if (!challengeId || !code) {
                        return null;
                    }

                    try {
                        const result = await verifyLoginChallenge(
                            { challengeId, code },
                            undefined,
                            { baseURL: apiBaseUrl }
                        );

                        if (!result.accessToken || !result.refreshToken) {
                            return null;
                        }

                        return {
                            id: challengeId,
                            accessToken: result.accessToken,
                            refreshToken: result.refreshToken,
                            accessTokenExpires: decodeJwtExpiration(
                                result.accessToken
                            ),
                        };
                    } catch {
                        return null;
                    }
                }

                const email = String(credentials?.email ?? "").trim();
                const password = String(credentials?.password ?? "");
                const rememberMe =
                    String(credentials?.rememberMe ?? "false") === "true";

                if (!email || !password) {
                    return null;
                }

                try {
                    const result = (await authenticate(
                        { email, password, rememberMe },
                        undefined,
                        { baseURL: apiBaseUrl }
                    )) as LoginResponse;

                    if (result.challengeId) {
                        throw new Error(`MFA_REQUIRED:${result.challengeId}`);
                    }

                    if (!result.accessToken || !result.refreshToken) {
                        return null;
                    }

                    return {
                        id: email,
                        email,
                        name: email.split("@")[0],
                        accessToken: result.accessToken,
                        refreshToken: result.refreshToken,
                        accessTokenExpires: decodeJwtExpiration(
                            result.accessToken
                        ),
                    };
                } catch (error: unknown) {
                    const message = extractErrorMessage(error);

                    if (message?.startsWith("MFA_REQUIRED")) {
                        throw error;
                    }

                    const status =
                        typeof error === "object" &&
                        error !== null &&
                        "response" in error &&
                        typeof error.response === "object" &&
                        error.response !== null &&
                        "status" in error.response
                            ? error.response.status
                            : undefined;

                    if (status === 202) {
                        const challengeId = extractChallengeId(error);

                        if (challengeId) {
                            throw new Error(`MFA_REQUIRED:${challengeId}`);
                        }

                        throw new Error("MFA_REQUIRED");
                    }

                    return null;
                }
            },
        }),
    ],
    callbacks: {
        async jwt({ token, user }) {
            if (user) {
                return {
                    ...token,
                    accessToken: user.accessToken,
                    refreshToken: user.refreshToken,
                    accessTokenExpires: user.accessTokenExpires,
                    error: undefined,
                };
            }

            if (
                token.accessToken &&
                token.accessTokenExpires &&
                Date.now() < token.accessTokenExpires
            ) {
                return token;
            }

            return refreshAccessToken(token);
        },
        async session({ session, token }) {
            session.error = token.error;
            session.accessToken = token.accessToken;
            session.accessTokenExpires = token.accessTokenExpires;
            return session;
        },
    },
};

export default NextAuth(authOptions);
