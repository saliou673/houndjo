import { axiosInstance, getConfig, setConfig } from "@kubb/plugin-client/clients/axios";

export const API_DOCS_URL = "http://localhost:8080/api/docs";

// Kubb output is generated under ./gen
export * from "./gen";
export { QueryClient, QueryClientProvider } from "@tanstack/react-query";
export { axiosInstance } from "@kubb/plugin-client/clients/axios";

/**
 * Serializes query params to the flat dot-notation format Spring MVC expects
 * for @ModelAttribute binding.
 *
 * The generated client wraps params as { filter: {...}, pageable: {...} }.
 * Spring binds these as individual query params WITHOUT the wrapper prefix,
 * e.g. email.contains=foo&status.equals=ACTIVE&page=0&size=10 - not
 * axios's default filter[email][contains]=foo&pageable[page]=0.
 */
function flattenToQueryString(value: unknown, prefix: string, parts: string[]): void {
    if (value === null || value === undefined) return;
    if (Array.isArray(value)) {
        for (const item of value) {
            if (item !== null && item !== undefined) {
                parts.push(`${encodeURIComponent(prefix)}=${encodeURIComponent(String(item))}`);
            }
        }
    } else if (typeof value === "object") {
        for (const [k, v] of Object.entries(value as Record<string, unknown>)) {
            if (v !== null && v !== undefined) {
                flattenToQueryString(v, prefix ? `${prefix}.${k}` : k, parts);
            }
        }
    } else {
        parts.push(`${encodeURIComponent(prefix)}=${encodeURIComponent(String(value))}`);
    }
}

function springParamsSerializer(params: unknown): string {
    if (!params || typeof params !== "object" || Array.isArray(params)) {
        return "";
    }
    const parts: string[] = [];
    for (const [key, value] of Object.entries(params as Record<string, unknown>)) {
        if (value === null || value === undefined) continue;
        if (typeof value === "object" && !Array.isArray(value)) {
            // Expand nested object WITHOUT the top-level key as prefix.
            // { filter: { email: { contains: "foo" } } } → email.contains=foo
            // { pageable: { page: 0, size: 10 } } → page=0&size=10
            for (const [nestedKey, nestedValue] of Object.entries(value as Record<string, unknown>)) {
                if (nestedValue !== null && nestedValue !== undefined) {
                    flattenToQueryString(nestedValue, nestedKey, parts);
                }
            }
        } else {
            flattenToQueryString(value, key, parts);
        }
    }
    return parts.join("&");
}

axiosInstance.defaults.paramsSerializer = springParamsSerializer;

type ApiClientConfig = {
    baseURL?: string;
    accessToken?: string;
};

export function configureApiClient(config: ApiClientConfig) {
    const current = getConfig();
    const currentHeaders = (current.headers ?? {}) as Record<string, string>;
    const headers = { ...currentHeaders };

    if (config.accessToken) {
        headers.Authorization = `Bearer ${config.accessToken}`;
    } else {
        delete headers.Authorization;
    }

    setConfig({
        ...current,
        baseURL: config.baseURL ?? current.baseURL,
        headers,
    });
}

export { useActiveCourseEnrollments } from "./course-enrollments";
