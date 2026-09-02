import type { UserPreferences } from "@api-client";

export type AppearancePreferenceValues = {
    theme: "light" | "dark" | "system";
    font: "inter" | "manrope" | "system";
};

export const defaultAppearancePreferenceValues: AppearancePreferenceValues = {
    theme: "system",
    font: "inter",
};

export function mapUserPreferencesToAppearanceValues(
    preferences?: UserPreferences | null
): AppearancePreferenceValues {
    const theme = preferences?.appearance?.theme?.toLowerCase();
    const font = preferences?.appearance?.font?.toLowerCase();

    return {
        theme:
            theme === "light" || theme === "dark" || theme === "system"
                ? theme
                : defaultAppearancePreferenceValues.theme,
        font:
            font === "inter" || font === "manrope" || font === "system"
                ? font
                : defaultAppearancePreferenceValues.font,
    };
}
