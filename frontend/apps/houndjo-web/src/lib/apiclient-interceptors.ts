"use client";

import {
    AxiosHeaders,
    type AxiosError,
    type InternalAxiosRequestConfig,
} from "axios";
import { axiosInstance } from "@api-client";
import { getSession } from "next-auth/react";

let currentAccessToken: string | undefined;
let interceptorsInstalled = false;
let refreshSessionPromise: Promise<string | undefined> | null = null;

type RetriableConfig = InternalAxiosRequestConfig & { _retry?: boolean };

export function setApiAccessToken(accessToken?: string) {
    currentAccessToken = accessToken;
}

async function refreshTokenFromSession(): Promise<string | undefined> {
    if (!refreshSessionPromise) {
        refreshSessionPromise = getSession()
            .then((session) => session?.accessToken)
            .finally(() => {
                refreshSessionPromise = null;
            });
    }

    const token = await refreshSessionPromise;
    setApiAccessToken(token);
    return token;
}

export function setupApiClientInterceptors(baseURL: string) {
    axiosInstance.defaults.baseURL = baseURL;

    if (interceptorsInstalled) {
        return;
    }

    interceptorsInstalled = true;

    axiosInstance.interceptors.request.use((config) => {
        const nextConfig = config;
        const headers = AxiosHeaders.from(nextConfig.headers);

        if (currentAccessToken) {
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

            if (
                typeof window === "undefined" ||
                status !== 401 ||
                !originalRequest ||
                originalRequest._retry
            ) {
                return Promise.reject(error);
            }

            originalRequest._retry = true;
            const refreshedToken = await refreshTokenFromSession();

            if (!refreshedToken) {
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
