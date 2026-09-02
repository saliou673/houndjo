import { Platform } from "react-native";
import * as SecureStore from "expo-secure-store";

export type AuthTokens = {
    accessToken: string;
    refreshToken: string;
};

const ACCESS_TOKEN_KEY = "auth.accessToken";
const REFRESH_TOKEN_KEY = "auth.refreshToken";

// expo-secure-store only supports Android/iOS/tvOS/Expo Go; fall back to an
// in-memory store on web so the app doesn't crash there. Tokens won't
// persist across web reloads, which is acceptable until a web-specific
// storage story is needed.
const isSecureStoreSupported = Platform.OS !== "web";
const memoryStore = new Map<string, string>();

async function getItem(key: string): Promise<string | null> {
    if (!isSecureStoreSupported) return memoryStore.get(key) ?? null;
    return SecureStore.getItemAsync(key);
}

async function setItem(key: string, value: string): Promise<void> {
    if (!isSecureStoreSupported) {
        memoryStore.set(key, value);
        return;
    }
    await SecureStore.setItemAsync(key, value);
}

async function deleteItem(key: string): Promise<void> {
    if (!isSecureStoreSupported) {
        memoryStore.delete(key);
        return;
    }
    await SecureStore.deleteItemAsync(key);
}

export async function getTokens(): Promise<AuthTokens | null> {
    const [accessToken, refreshToken] = await Promise.all([
        getItem(ACCESS_TOKEN_KEY),
        getItem(REFRESH_TOKEN_KEY),
    ]);

    if (!accessToken || !refreshToken) return null;

    return { accessToken, refreshToken };
}

export async function setTokens(tokens: AuthTokens): Promise<void> {
    await Promise.all([
        setItem(ACCESS_TOKEN_KEY, tokens.accessToken),
        setItem(REFRESH_TOKEN_KEY, tokens.refreshToken),
    ]);
}

export async function clearTokens(): Promise<void> {
    await Promise.all([
        deleteItem(ACCESS_TOKEN_KEY),
        deleteItem(REFRESH_TOKEN_KEY),
    ]);
}
