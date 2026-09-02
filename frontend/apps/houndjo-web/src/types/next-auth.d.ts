import "next-auth";
import "next-auth/jwt";

declare module "next-auth" {
    interface Session {
        accessToken?: string;
        accessTokenExpires?: number;
        error?: "RefreshAccessTokenError";
    }

    interface User {
        accessToken: string;
        refreshToken: string;
        accessTokenExpires: number;
    }
}

declare module "next-auth/jwt" {
    interface JWT {
        accessToken?: string;
        refreshToken?: string;
        accessTokenExpires?: number;
        error?: "RefreshAccessTokenError";
    }
}
