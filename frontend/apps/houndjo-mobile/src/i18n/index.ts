import AsyncStorage from "@react-native-async-storage/async-storage";
import { getLocales } from "expo-localization";
import i18n from "i18next";
import { initReactI18next } from "react-i18next";

import en from "./locales/en.json";
import fr from "./locales/fr.json";

export const SUPPORTED_LANGUAGES = ["en", "fr"] as const;
export type SupportedLanguage = (typeof SUPPORTED_LANGUAGES)[number];

const LANGUAGE_STORAGE_KEY = "houndjo-mobile-language";

function isSupportedLanguage(
    value: string | null | undefined
): value is SupportedLanguage {
    return (
        !!value && (SUPPORTED_LANGUAGES as readonly string[]).includes(value)
    );
}

function resolveDeviceLanguage(): SupportedLanguage {
    const [locale] = getLocales();

    return isSupportedLanguage(locale?.languageCode)
        ? (locale.languageCode as SupportedLanguage)
        : "en";
}

void i18n.use(initReactI18next).init({
    resources: {
        en: { translation: en },
        fr: { translation: fr },
    },
    lng: resolveDeviceLanguage(),
    fallbackLng: "en",
    interpolation: { escapeValue: false },
});

// AsyncStorage touches `window`/native modules, so it must not run at module
// import time (breaks static export's Node-side prerendering, like the
// `_layout.tsx` server render); call this from a client-only effect instead,
// mirroring how `hydrateAccessToken` is called in `_layout.tsx`.
export async function hydrateStoredLanguage(): Promise<void> {
    const stored = await AsyncStorage.getItem(LANGUAGE_STORAGE_KEY);

    if (isSupportedLanguage(stored) && stored !== i18n.language) {
        await i18n.changeLanguage(stored);
    }
}

export async function setLanguage(language: SupportedLanguage): Promise<void> {
    await AsyncStorage.setItem(LANGUAGE_STORAGE_KEY, language);
    await i18n.changeLanguage(language);
}

export default i18n;
