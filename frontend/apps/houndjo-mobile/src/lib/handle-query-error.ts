import { AxiosError } from "axios";
import { router, type Href } from "expo-router";
import i18n from "@/i18n";
import { showToast } from "@/components/toast/toast-store";
import { extractApiErrorMessage } from "@/lib/api-error";

type HandleQueryErrorOptions = {
    /**
     * Queries are page-level reads: a 403 means the user can't view this
     * screen at all, so it navigates to the dedicated forbidden screen
     * instead of toasting - mirrors the web app's requirePermission()
     * server-side redirect. Mutations stay toast-only (a 403 there means a
     * specific action was rejected, not that the whole screen is off
     * limits), so this defaults to false for those.
     */
    navigateOnForbidden?: boolean;
};

/**
 * Issue #5's interceptor already retries once on 401 and force-logs-out on
 * refresh failure; a 401 reaching here means that has already happened, so
 * this only needs to surface a message, not repeat the auth flow.
 */
export function handleQueryError(error: unknown, options: HandleQueryErrorOptions = {}): void {
    if (__DEV__) {
        console.log(error);
    }

    if (error instanceof AxiosError) {
        const status = error.response?.status;

        if (status === 401) {
            showToast(i18n.t("errors.sessionExpired"), "error");
            return;
        }

        if (status === 403 && options.navigateOnForbidden) {
            router.push("/errors/forbidden" as Href);
            return;
        }

        if (status !== undefined && status >= 500) {
            showToast(i18n.t("errors.serverError"), "error");
            return;
        }

        showToast(extractApiErrorMessage(error, i18n.t("errors.generic")), "error");
        return;
    }

    showToast(i18n.t("errors.generic"), "error");
}
