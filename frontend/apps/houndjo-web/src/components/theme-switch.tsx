import { useEffect } from "react";
import { useQueryClient } from "@tanstack/react-query";
import {
    appearancePreferencesFontEnum,
    appearancePreferencesThemeEnum,
    displayPreferencesTextSizeEnum,
    getCurrentUserPreferencesQueryKey,
    useGetCurrentUserPreferences,
    useUpdateCurrentUserPreferences,
} from "@api-client";
import { Check, Moon, Sun } from "lucide-react";
import { useSession } from "next-auth/react";
import { cn } from "@/lib/utils";
import { useFont } from "@/context/font-provider";
import { useTheme } from "@/context/theme-provider";
import { Button } from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

type ThemeOption = "light" | "dark" | "system";

export function ThemeSwitch() {
    const { theme, setTheme } = useTheme();
    const { font } = useFont();
    const { status } = useSession();
    const queryClient = useQueryClient();

    const { data: preferences } = useGetCurrentUserPreferences(undefined, {
        query: { enabled: status === "authenticated" },
    });
    const { mutate: updatePreferences } = useUpdateCurrentUserPreferences();

    const handleSetTheme = (newTheme: ThemeOption) => {
        setTheme(newTheme);

        if (status !== "authenticated") return;

        const themeKey =
            newTheme.toUpperCase() as keyof typeof appearancePreferencesThemeEnum;
        const fontKey =
            font.toUpperCase() as keyof typeof appearancePreferencesFontEnum;

        updatePreferences(
            {
                data: {
                    appearance: {
                        theme: appearancePreferencesThemeEnum[themeKey],
                        font: appearancePreferencesFontEnum[fontKey],
                    },
                    // This mutation replaces the whole preferences document,
                    // so the rest has to be carried through unchanged here.
                    notifications: preferences?.notifications ?? {
                        productUpdatesEnabled: false,
                    },
                    display: preferences?.display ?? {
                        textSize: displayPreferencesTextSizeEnum.DEFAULT,
                        reduceMotion: false,
                    },
                },
            },
            {
                onSuccess: () => {
                    queryClient.invalidateQueries({
                        queryKey: getCurrentUserPreferencesQueryKey(),
                    });
                },
            }
        );
    };

    /* Update theme-color meta tag
     * when theme is updated */
    useEffect(() => {
        const themeColor = theme === "dark" ? "#020817" : "#fff";
        const metaThemeColor = document.querySelector(
            "meta[name='theme-color']"
        );
        if (metaThemeColor) metaThemeColor.setAttribute("content", themeColor);
    }, [theme]);

    return (
        <DropdownMenu modal={false}>
            <DropdownMenuTrigger asChild>
                <Button
                    variant="ghost"
                    size="icon"
                    className="scale-95 rounded-full"
                >
                    <Sun className="size-[1.2rem] scale-100 rotate-0 transition-all dark:scale-0 dark:-rotate-90" />
                    <Moon className="absolute size-[1.2rem] scale-0 rotate-90 transition-all dark:scale-100 dark:rotate-0" />
                    <span className="sr-only">Toggle theme</span>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                <DropdownMenuItem onSelect={() => handleSetTheme("light")}>
                    Light{" "}
                    <Check
                        size={14}
                        className={cn("ms-auto", theme !== "light" && "hidden")}
                    />
                </DropdownMenuItem>
                <DropdownMenuItem onSelect={() => handleSetTheme("dark")}>
                    Dark
                    <Check
                        size={14}
                        className={cn("ms-auto", theme !== "dark" && "hidden")}
                    />
                </DropdownMenuItem>
                <DropdownMenuItem onSelect={() => handleSetTheme("system")}>
                    System
                    <Check
                        size={14}
                        className={cn(
                            "ms-auto",
                            theme !== "system" && "hidden"
                        )}
                    />
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
