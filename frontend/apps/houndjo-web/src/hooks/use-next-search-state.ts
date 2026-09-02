"use client";

import { useMemo } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import type { NavigateFn } from "@/hooks/use-table-url-state";

type SearchRecord = Record<string, unknown>;

function parseValue(value: string): unknown {
    if (value === "true") return true;
    if (value === "false") return false;
    if (/^-?\d+(\.\d+)?$/.test(value)) return Number(value);
    return value;
}

export function useNextSearchObject(): SearchRecord {
    const searchParams = useSearchParams();

    return useMemo(() => {
        const output: SearchRecord = {};

        for (const [key, value] of searchParams.entries()) {
            const parsed = parseValue(value);
            const existing = output[key];
            if (existing === undefined) {
                output[key] = parsed;
                continue;
            }
            output[key] = Array.isArray(existing)
                ? [...existing, parsed]
                : [existing, parsed];
        }

        return output;
    }, [searchParams]);
}

export function useNextNavigateSearch(): NavigateFn {
    const router = useRouter();
    const pathname = usePathname();
    const search = useNextSearchObject();

    return ({ search: nextSearch, replace }) => {
        const resolved =
            typeof nextSearch === "function"
                ? (nextSearch(search) as SearchRecord)
                : ((nextSearch === true ? search : nextSearch) as SearchRecord);

        const params = new URLSearchParams();
        for (const [key, value] of Object.entries(resolved)) {
            if (value === undefined || value === null || value === "") continue;
            if (Array.isArray(value)) {
                for (const item of value) {
                    if (item === undefined || item === null || item === "")
                        continue;
                    params.append(key, String(item));
                }
                continue;
            }
            params.set(key, String(value));
        }

        const query = params.toString();
        const url = query ? `${pathname}?${query}` : pathname;
        if (replace) {
            router.replace(url);
            return;
        }
        router.push(url);
    };
}
