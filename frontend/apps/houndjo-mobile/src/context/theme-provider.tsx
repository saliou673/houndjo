import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { useColorScheme } from "@/hooks/use-color-scheme";

export type Theme = "dark" | "light" | "system";
type ResolvedTheme = Exclude<Theme, "system">;

const DEFAULT_THEME: Theme = "system";
const THEME_STORAGE_KEY = "houndjo-mobile-theme";

type ThemeProviderProps = {
    children: ReactNode;
    defaultTheme?: Theme;
    storageKey?: string;
};

type ThemeProviderState = {
    defaultTheme: Theme;
    resolvedTheme: ResolvedTheme;
    theme: Theme;
    setTheme: (theme: Theme) => void;
    resetTheme: () => void;
};

const initialState: ThemeProviderState = {
    defaultTheme: DEFAULT_THEME,
    resolvedTheme: "light",
    theme: DEFAULT_THEME,
    setTheme: () => null,
    resetTheme: () => null,
};

const ThemeContext = createContext<ThemeProviderState>(initialState);

function isTheme(value: string | null): value is Theme {
    return value === "dark" || value === "light" || value === "system";
}

export function AppThemeProvider({
    children,
    defaultTheme = DEFAULT_THEME,
    storageKey = THEME_STORAGE_KEY,
}: ThemeProviderProps) {
    const systemColorScheme = useColorScheme();
    const [theme, _setTheme] = useState<Theme>(defaultTheme);

    // AsyncStorage has no synchronous read, so the persisted choice can only
    // be applied after mount; until then `theme` stays at `defaultTheme`.
    useEffect(() => {
        AsyncStorage.getItem(storageKey).then((stored) => {
            if (isTheme(stored)) {
                _setTheme(stored);
            }
        });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []); // intentionally run once on mount only

    const resolvedTheme = useMemo((): ResolvedTheme => {
        if (theme === "system") {
            return systemColorScheme === "dark" ? "dark" : "light";
        }
        return theme;
    }, [theme, systemColorScheme]);

    const setTheme = useCallback(
        (nextTheme: Theme) => {
            _setTheme(nextTheme);
            void AsyncStorage.setItem(storageKey, nextTheme);
        },
        [storageKey]
    );

    const resetTheme = useCallback(() => {
        _setTheme(DEFAULT_THEME);
        void AsyncStorage.removeItem(storageKey);
    }, [storageKey]);

    const contextValue = useMemo(
        () => ({
            defaultTheme,
            resolvedTheme,
            resetTheme,
            theme,
            setTheme,
        }),
        [defaultTheme, resolvedTheme, resetTheme, theme, setTheme]
    );

    return (
        <ThemeContext value={contextValue}>{children}</ThemeContext>
    );
}

export const useAppTheme = () => {
    const context = useContext(ThemeContext);

    if (!context) {
        throw new Error("useAppTheme must be used within an AppThemeProvider");
    }

    return context;
};
