import {
    AxiosHeaders,
    type AxiosError,
    type InternalAxiosRequestConfig,
} from "axios";
import { axiosInstance, refreshToken as requestRefreshToken } from "@api-client";
import { clearTokens, getTokens, setTokens } from "@/lib/auth-storage";

type RetriableConfig = InternalAxiosRequestConfig & { _retry?: boolean };

const REFRESH_TOKEN_PATH = "/api/auth/refresh";

function isRefreshTokenRequest(config: { url?: string } | undefined): boolean {
    return Boolean(config?.url?.includes(REFRESH_TOKEN_PATH));
}

let currentAccessToken: string | undefined;
let interceptorsInstalled = false;
let refreshPromise: Promise<string | undefined> | null = null;
let forceLogoutPromise: Promise<void> | null = null;
let onForceLogout: (() => void) | undefined;

export function setApiAccessToken(accessToken?: string) {
    currentAccessToken = accessToken;
}

/**
 * Issue #6 (navigation shell) hooks in here to redirect to sign-in once
 * tokens are cleared — kept decoupled so this module has no router dependency.
 */
export function setOnForceLogout(callback: (() => void) | undefined) {
    onForceLogout = callback;
}

export async function hydrateAccessToken(): Promise<void> {
    const stored = await getTokens();
    setApiAccessToken(stored?.accessToken);
}

async function forceLogout(): Promise<void> {
    if (!forceLogoutPromise) {
        forceLogoutPromise = (async () => {
            setApiAccessToken(undefined);
            await clearTokens();
            onForceLogout?.();
        })().finally(() => {
            forceLogoutPromise = null;
        });
    }

    return forceLogoutPromise;
}

async function refreshAccessToken(): Promise<string | undefined> {
    if (!refreshPromise) {
        refreshPromise = (async () => {
            const stored = await getTokens();
            if (!stored) return undefined;

            try {
                const refreshed = await requestRefreshToken(stored.refreshToken);

                if (!refreshed.accessToken || !refreshed.refreshToken) {
                    return undefined;
                }

                await setTokens({
                    accessToken: refreshed.accessToken,
                    refreshToken: refreshed.refreshToken,
                });
                setApiAccessToken(refreshed.accessToken);
                return refreshed.accessToken;
            } catch {
                return undefined;
            }
        })().finally(() => {
            refreshPromise = null;
        });
    }

    return refreshPromise;
}

export function setupAuthInterceptor() {
    if (interceptorsInstalled) {
        return;
    }

    interceptorsInstalled = true;

    axiosInstance.interceptors.request.use((config) => {
        const nextConfig = config;
        const headers = AxiosHeaders.from(nextConfig.headers);

        // The refresh endpoint takes the refresh token in its body and is
        // public - it doesn't need the (possibly expired) access token as a
        // Bearer header. Sending it anyway lets Spring's resource-server
        // filter reject the request with 401 before the refresh logic runs,
        // which would recurse back into this same refresh flow and deadlock.
        if (currentAccessToken && !isRefreshTokenRequest(nextConfig)) {
            headers.set("Authorization", `Bearer ${currentAccessToken}`);
        } else {
            headers.delete("Authorization");
        }

        nextConfig.headers = headers;
        return nextConfig;
    });

    axiosInstance.interceptors.response.use(
        (response) => response,
        async (error: AxiosError) => {
            const status = error.response?.status;
            const originalRequest = error.config as RetriableConfig | undefined;

            // A 401 from the refresh call itself must not trigger another
            // refresh attempt - refreshAccessToken() below is already
            // awaiting this exact request, so recursing here would await
            // that same in-flight promise and deadlock forever.
            if (
                status !== 401 ||
                !originalRequest ||
                originalRequest._retry ||
                isRefreshTokenRequest(originalRequest)
            ) {
                return Promise.reject(error);
            }

            originalRequest._retry = true;

            const refreshedToken = await refreshAccessToken();

            if (!refreshedToken) {
                await forceLogout();
                return Promise.reject(error);
            }

            originalRequest.headers = AxiosHeaders.from(
                originalRequest.headers
            );
            originalRequest.headers.set(
                "Authorization",
                `Bearer ${refreshedToken}`
            );

            return axiosInstance(originalRequest);
        }
    );
}
