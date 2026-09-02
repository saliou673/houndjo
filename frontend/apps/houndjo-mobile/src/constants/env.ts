import Constants from "expo-constants";
import type { AppEnv } from "../../app.config";

type AppExtra = {
    appEnv?: AppEnv;
    apiBaseUrl?: string;
};

const extra = (Constants.expoConfig?.extra ?? {}) as AppExtra;

export const appEnv: AppEnv = extra.appEnv ?? "development";

const configuredApiBaseUrl = extra.apiBaseUrl ?? "http://localhost:8080";

/**
 * On a physical device `localhost` resolves to the phone, not the dev machine,
 * so every request fails before reaching the API. `hostUri` is only populated by
 * @expo/cli during development and holds the address the client used to reach
 * the dev server — the same machine the API runs on.
 */
function resolveDevHost(url: string): string {
    const hostUri = Constants.expoConfig?.hostUri;
    if (!hostUri) {
        return url;
    }

    const devHost = hostUri.split("/")[0].split(":")[0];
    return url.replace(
        /^(https?:\/\/)(?:localhost|127\.0\.0\.1)(?=[:/]|$)/,
        `$1${devHost}`
    );
}

export const apiBaseUrl: string = resolveDevHost(configuredApiBaseUrl);
