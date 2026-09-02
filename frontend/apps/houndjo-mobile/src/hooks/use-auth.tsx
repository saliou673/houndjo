import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from "react";
import { router, type Href } from "expo-router";

import {
    clearTokens,
    getTokens,
    setTokens,
    type AuthTokens,
} from "@/lib/auth-storage";
import { setApiAccessToken, setOnForceLogout } from "@/lib/auth-interceptor";

type AuthContextValue = {
    isLoading: boolean;
    isAuthenticated: boolean;
    signIn: (tokens: AuthTokens) => Promise<void>;
    signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
    const [isLoading, setIsLoading] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [sessionExpired, setSessionExpired] = useState(false);

    useEffect(() => {
        let cancelled = false;

        void (async () => {
            const stored = await getTokens();
            if (cancelled) return;
            setIsAuthenticated(stored !== null);
            setIsLoading(false);
        })();

        return () => {
            cancelled = true;
        };
    }, []);

    useEffect(() => {
        setOnForceLogout(() => {
            setSessionExpired(true);
            setIsAuthenticated(false);
        });
        return () => setOnForceLogout(undefined);
    }, []);

    // Waits for the isAuthenticated flip above to commit and mount the
    // (auth) group before navigating, rather than calling router.replace
    // synchronously from the force-logout callback where that group may not
    // exist in the navigation tree yet. sessionExpired is cleared on the
    // next successful sign-in (not here), so a later deliberate signOut()
    // doesn't re-trigger this navigation.
    useEffect(() => {
        if (sessionExpired && !isAuthenticated) {
            router.replace("/session-expired" as Href);
        }
    }, [sessionExpired, isAuthenticated]);

    const signIn = useCallback(async (tokens: AuthTokens) => {
        await setTokens(tokens);
        setApiAccessToken(tokens.accessToken);
        setSessionExpired(false);
        setIsAuthenticated(true);
    }, []);

    const signOut = useCallback(async () => {
        await clearTokens();
        setApiAccessToken(undefined);
        setIsAuthenticated(false);
    }, []);

    const value = useMemo<AuthContextValue>(
        () => ({ isLoading, isAuthenticated, signIn, signOut }),
        [isLoading, isAuthenticated, signIn, signOut]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }

    return context;
}
