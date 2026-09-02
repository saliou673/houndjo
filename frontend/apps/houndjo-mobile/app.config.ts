import type { ConfigContext, ExpoConfig } from "expo/config";

export type AppEnv = "development" | "staging" | "production";

// Derived from houndjo-web's light-mode `--primary` theme token (oklch(0.208 0.042 265.755)).
const BRAND_COLOR = "#0f172b";

const DEFAULT_API_BASE_URLS: Record<AppEnv, string> = {
    development: "http://localhost:8080",
    staging: "https://staging-api.houndjo.dev",
    production: "https://api.houndjo.dev",
};

// The houndjo-web origin whose auth email links (password reset, invitation,
// activation) universal/app links should be verified against, so tapping
// one on-device opens this app instead of a browser.
const DEFAULT_WEB_APP_ORIGINS: Record<AppEnv, string> = {
    development: "http://localhost:3000",
    staging: "https://staging-app.houndjo.dev",
    production: "https://app.houndjo.dev",
};

// Paths houndjo-web serves for the auth email flows, mirrored 1:1 by routes
// under `src/app/(auth)` so a universal link and the `houndjomobile://`
// custom scheme resolve to the same screen.
const AUTH_LINK_PATHS = ["/reset-password", "/account/invitation", "/activate"];

// Shared with android.package below so both platforms register under the
// same reverse-domain identifier.
const BUNDLE_IDENTIFIER = "com.houndjo.mobile";

function resolveAppEnv(): AppEnv {
    const raw = process.env.APP_ENV ?? process.env.EAS_BUILD_PROFILE;

    if (raw === "production" || raw === "staging") {
        return raw;
    }

    return "development";
}

export default ({ config }: ConfigContext): ExpoConfig => {
    const appEnv = resolveAppEnv();
    const apiBaseUrl =
        process.env.EXPO_PUBLIC_API_BASE_URL ?? DEFAULT_API_BASE_URLS[appEnv];
    const webAppOrigin =
        process.env.EXPO_PUBLIC_WEB_APP_ORIGIN ?? DEFAULT_WEB_APP_ORIGINS[appEnv];
    const webAppHost = webAppOrigin.replace(/^https?:\/\//, "");

    return {
        ...config,
        name: "Houndjo",
        slug: "houndjo-mobile",
        version: "1.0.0",
        orientation: "portrait",
        icon: "./assets/images/icon.png",
        scheme: "houndjomobile",
        userInterfaceStyle: "automatic",
        ios: {
            icon: "./assets/images/icon.png",
            bundleIdentifier: BUNDLE_IDENTIFIER,
            // Requires `.well-known/apple-app-site-association` served from
            // webAppHost, listing this app's team/bundle ID, before iOS will
            // open links to these paths in the app instead of Safari.
            associatedDomains: [`applinks:${webAppHost}`],
        },
        android: {
            adaptiveIcon: {
                backgroundColor: BRAND_COLOR,
                foregroundImage: "./assets/images/android-icon-foreground.png",
                backgroundImage: "./assets/images/android-icon-background.png",
                monochromeImage: "./assets/images/android-icon-monochrome.png",
            },
            predictiveBackGestureEnabled: false,
            package: BUNDLE_IDENTIFIER,
            // Requires `.well-known/assetlinks.json` served from webAppHost,
            // listing this app's package/signing cert, before Android will
            // verify the link and skip the disambiguation prompt.
            intentFilters: [
                {
                    action: "VIEW",
                    autoVerify: true,
                    data: AUTH_LINK_PATHS.map((pathPrefix) => ({
                        scheme: "https",
                        host: webAppHost,
                        pathPrefix,
                    })),
                    category: ["BROWSABLE", "DEFAULT"],
                },
            ],
        },
        web: {
            output: "static",
            favicon: "./assets/images/favicon.png",
        },
        plugins: [
            "expo-router",
            "expo-secure-store",
            "expo-localization",
            "expo-font",
            "expo-image",
            "expo-web-browser",
            [
                "expo-splash-screen",
                {
                    backgroundColor: BRAND_COLOR,
                    image: "./assets/images/splash-icon.png",
                    imageWidth: 160,
                    android: {
                        image: "./assets/images/splash-icon.png",
                        imageWidth: 160,
                    },
                },
            ],
        ],
        experiments: {
            typedRoutes: true,
            reactCompiler: true,
        },
        extra: {
            ...config.extra,
            appEnv,
            apiBaseUrl,
        },
    };
};
