"use client";

import { useEffect, useRef } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { getUserDetailsQueryKey, useGetUserDetails } from "@api-client";
import { useSession } from "next-auth/react";

interface CurrentUser {
    name: string;
    email: string;
    avatar?: string;
    initials: string;
}

function buildDisplayName(
    firstName?: string,
    lastName?: string,
    fallbackName?: string,
    fallbackEmail?: string | null
) {
    const fullName = [firstName, lastName].filter(Boolean).join(" ").trim();

    if (fullName) return fullName;
    if (fallbackName) return fallbackName;
    if (fallbackEmail) return fallbackEmail.split("@")[0] ?? "User";

    return "User";
}

function buildInitials(name: string, email: string) {
    const fromName = name
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0]?.toUpperCase() ?? "")
        .join("");

    if (fromName) return fromName;

    return (email[0] ?? "U").toUpperCase();
}

export function useCurrentUser() {
    const { data: session, status } = useSession();
    const queryClient = useQueryClient();
    const sessionEmail = session?.user?.email ?? null;
    const previousSessionEmail = useRef<string | null>(sessionEmail);

    useEffect(() => {
        if (previousSessionEmail.current === sessionEmail) {
            return;
        }

        previousSessionEmail.current = sessionEmail;
        queryClient.removeQueries({ queryKey: getUserDetailsQueryKey() });
    }, [queryClient, sessionEmail]);

    const query = useGetUserDetails(undefined, {
        query: {
            enabled: status === "authenticated",
        },
    });

    const safeQueryData =
        status === "authenticated" &&
        sessionEmail &&
        query.data?.email === sessionEmail
            ? query.data
            : undefined;

    const email = safeQueryData?.email ?? session?.user?.email ?? "";
    const name = buildDisplayName(
        safeQueryData?.firstName,
        safeQueryData?.lastName,
        session?.user?.name ?? undefined,
        sessionEmail
    );

    const user: CurrentUser = {
        name,
        email,
        avatar: safeQueryData?.imageUrl,
        initials: buildInitials(name, email),
    };

    return {
        ...query,
        user,
    };
}
