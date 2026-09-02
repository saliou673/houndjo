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
import { displayPreferencesTextSizeEnum, type DisplayPreferencesTextSizeEnumKey } from "@api-client";

export type TextSize = DisplayPreferencesTextSizeEnumKey;

const DEFAULT_TEXT_SIZE: TextSize = displayPreferencesTextSizeEnum.DEFAULT;
const TEXT_SIZE_STORAGE_KEY = "houndjo-mobile-text-size";

// Multiplier applied to every ThemedText fontSize/lineHeight.
export const TEXT_SIZE_SCALE: Record<TextSize, number> = {
    SMALL: 0.9,
    DEFAULT: 1,
    LARGE: 1.15,
};

type TextSizeProviderProps = {
    children: ReactNode;
};

type TextSizeProviderState = {
    textSize: TextSize;
    scale: number;
    setTextSize: (textSize: TextSize) => void;
};

const initialState: TextSizeProviderState = {
    textSize: DEFAULT_TEXT_SIZE,
    scale: TEXT_SIZE_SCALE[DEFAULT_TEXT_SIZE],
    setTextSize: () => null,
};

const TextSizeContext = createContext<TextSizeProviderState>(initialState);

function isTextSize(value: string | null): value is TextSize {
    return value === "SMALL" || value === "DEFAULT" || value === "LARGE";
}

export function AppTextSizeProvider({ children }: TextSizeProviderProps) {
    const [textSize, _setTextSize] = useState<TextSize>(DEFAULT_TEXT_SIZE);

    // AsyncStorage has no synchronous read, so the persisted choice can only
    // be applied after mount; until then `textSize` stays at the default.
    // The account's stored preference (fetched on the settings screen)
    // overrides this once it's known, same as AppThemeProvider does for theme.
    useEffect(() => {
        AsyncStorage.getItem(TEXT_SIZE_STORAGE_KEY).then((stored) => {
            if (isTextSize(stored)) {
                _setTextSize(stored);
            }
        });
    }, []); // intentionally run once on mount only

    const setTextSize = useCallback((next: TextSize) => {
        _setTextSize(next);
        void AsyncStorage.setItem(TEXT_SIZE_STORAGE_KEY, next);
    }, []);

    const contextValue = useMemo(
        () => ({
            textSize,
            scale: TEXT_SIZE_SCALE[textSize],
            setTextSize,
        }),
        [textSize, setTextSize]
    );

    return <TextSizeContext value={contextValue}>{children}</TextSizeContext>;
}

export const useTextSize = () => {
    const context = useContext(TextSizeContext);

    if (!context) {
        throw new Error("useTextSize must be used within an AppTextSizeProvider");
    }

    return context;
};
