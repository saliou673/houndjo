import { authOptions } from "@/auth";
import { getCurrentUserPermissions, type Permission } from "@api-client";
import { getServerSession } from "next-auth";
import { redirect } from "next/navigation";
import "server-only";

const apiBaseUrl =
    process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

function getErrorStatus(error: unknown): number | undefined {
    if (
        typeof error === "object" &&
        error !== null &&
        "response" in error &&
        typeof error.response === "object" &&
        error.response !== null &&
        "status" in error.response &&
        typeof error.response.status === "number"
    ) {
        return error.response.status;
    }

    return undefined;
}

function hasPermission(
    permissions: Permission[],
    requiredCode: string
): boolean {
    return permissions.some((permission) => permission.code === requiredCode);
}

export async function requirePermission(requiredCode: string) {
    const session = await getServerSession(authOptions);

    if (!session?.accessToken) {
        redirect("/sign-in");
    }

    try {
        const permissions = await getCurrentUserPermissions(undefined, {
            baseURL: apiBaseUrl,
            headers: {
                Authorization: `Bearer ${session.accessToken}`,
            },
        });

        if (!hasPermission(permissions, requiredCode)) {
            redirect("/errors/forbidden");
        }

        return { session, permissions };
    } catch (error) {
        const status = getErrorStatus(error);

        if (status === 401) {
            redirect("/sign-in");
        }

        if (status === 403) {
            redirect("/errors/forbidden");
        }

        throw error;
    }
}
