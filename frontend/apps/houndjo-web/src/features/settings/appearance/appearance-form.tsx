import { useEffect } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { ChevronDownIcon } from "@radix-ui/react-icons";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import { fonts } from "@/config/fonts";
import {
    appearancePreferencesFontEnum,
    appearancePreferencesThemeEnum,
    displayPreferencesTextSizeEnum,
    getCurrentUserPreferencesQueryKey,
    getUserDetailsQueryKey,
    type DisplayPreferences,
    type NotificationPreferences,
    type UserSummary,
    type UserPreferences,
    useGetCurrentUserPreferences,
    useUpdateCurrentUserPreferences,
} from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import {
    defaultAppearancePreferenceValues,
    mapUserPreferencesToAppearanceValues,
} from "@/lib/user-preferences";
import { cn } from "@/lib/utils";
import { useFont } from "@/context/font-provider";
import { useTheme } from "@/context/theme-provider";
import { Button, buttonVariants } from "@/components/ui/button";
import {
    Form,
    FormControl,
    FormDescription,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Skeleton } from "@/components/ui/skeleton";

const appearanceFormSchema = z.object({
    theme: z.enum(["light", "dark", "system"]),
    font: z.enum(fonts),
});

type AppearanceFormValues = z.infer<typeof appearanceFormSchema>;

const defaultValues: AppearanceFormValues = defaultAppearancePreferenceValues;

const defaultNotificationPreferences: NotificationPreferences = {
    productUpdatesEnabled: false,
};

const defaultDisplayPreferences: DisplayPreferences = {
    textSize: displayPreferencesTextSizeEnum.DEFAULT,
    reduceMotion: false,
};

function mapApiPreferencesToFormValues(
    preferences?: UserPreferences | null
): AppearanceFormValues {
    return mapUserPreferencesToAppearanceValues(preferences);
}

function toApiPreferences(
    values: AppearanceFormValues,
    currentNotifications: NotificationPreferences,
    currentDisplay: DisplayPreferences
): UserPreferences {
    return {
        appearance: {
            theme: appearancePreferencesThemeEnum[
                values.theme.toUpperCase() as keyof typeof appearancePreferencesThemeEnum
            ],
            font: appearancePreferencesFontEnum[
                values.font.toUpperCase() as keyof typeof appearancePreferencesFontEnum
            ],
        },
        // This mutation replaces the whole preferences document, so the
        // rest has to be carried through unchanged here.
        notifications: currentNotifications,
        display: currentDisplay,
    };
}

function AppearanceFormSkeleton() {
    return (
        <div className="space-y-4">
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-52 w-full" />
        </div>
    );
}

type ThemeItemProps = {
    value: AppearanceFormValues["theme"];
    label: string;
    previewClassName: string;
    children: React.ReactNode;
};

function ThemeItem({
    value,
    label,
    previewClassName,
    children,
}: ThemeItemProps) {
    return (
        <FormItem>
            <FormLabel className="flex cursor-pointer flex-col [&:has([data-state=checked])>div]:border-primary">
                <FormControl>
                    <RadioGroupItem value={value} className="sr-only" />
                </FormControl>
                <div
                    className={cn(
                        "rounded-md border-2 border-muted p-1",
                        previewClassName
                    )}
                >
                    {children}
                </div>
                <span className="block w-full p-2 text-center font-normal">
                    {label}
                </span>
            </FormLabel>
        </FormItem>
    );
}

export function AppearanceForm() {
    const t = useTranslations("SettingsAppearance.form");
    const queryClient = useQueryClient();
    const { setFont } = useFont();
    const { setTheme } = useTheme();
    const {
        data: preferences,
        isLoading: isLoadingPreferences,
        isError: isPreferencesError,
    } = useGetCurrentUserPreferences();

    const { mutate: updatePreferences, isPending: isUpdatingPreferences } =
        useUpdateCurrentUserPreferences({
            mutation: {
                onSuccess: (updatedPreferences) => {
                    const nextValues =
                        mapApiPreferencesToFormValues(updatedPreferences);
                    setTheme(nextValues.theme);
                    setFont(nextValues.font);
                    form.reset(nextValues);
                    queryClient.setQueryData(
                        getCurrentUserPreferencesQueryKey(),
                        updatedPreferences
                    );
                    queryClient.setQueryData(
                        getUserDetailsQueryKey(),
                        (currentUser: UserSummary | undefined) =>
                            currentUser
                                ? {
                                      ...currentUser,
                                      preferences: updatedPreferences,
                                  }
                                : currentUser
                    );
                    toast.success(t("toastUpdated"));
                },
                onError: handleServerError,
            },
        });

    const form = useForm<AppearanceFormValues>({
        resolver: zodResolver(appearanceFormSchema),
        defaultValues,
    });

    useEffect(() => {
        if (!preferences) return;
        const nextValues = mapApiPreferencesToFormValues(preferences);
        const currentValues = form.getValues();

        if (
            currentValues.theme !== nextValues.theme ||
            currentValues.font !== nextValues.font
        ) {
            form.reset(nextValues);
        }

        setTheme(nextValues.theme);
        setFont(nextValues.font);
    }, [form, preferences, setFont, setTheme]);

    function onSubmit(data: AppearanceFormValues) {
        updatePreferences({
            data: toApiPreferences(
                data,
                preferences?.notifications ?? defaultNotificationPreferences,
                preferences?.display ?? defaultDisplayPreferences
            ),
        });
    }

    if (isLoadingPreferences) {
        return <AppearanceFormSkeleton />;
    }

    if (isPreferencesError) {
        return (
            <p className="text-sm text-muted-foreground">{t("loadError")}</p>
        );
    }

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
                <FormField
                    control={form.control}
                    name="font"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("fontLabel")}</FormLabel>
                            <div className="relative w-max">
                                <FormControl>
                                    <select
                                        className={cn(
                                            buttonVariants({
                                                variant: "outline",
                                            }),
                                            "w-[200px] appearance-none font-normal capitalize",
                                            "dark:bg-background dark:hover:bg-background"
                                        )}
                                        {...field}
                                    >
                                        {fonts.map((font) => (
                                            <option key={font} value={font}>
                                                {font}
                                            </option>
                                        ))}
                                    </select>
                                </FormControl>
                                <ChevronDownIcon className="absolute end-3 top-2.5 h-4 w-4 opacity-50" />
                            </div>
                            <FormDescription className="font-manrope">
                                {t("fontDescription")}
                            </FormDescription>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="theme"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("themeLabel")}</FormLabel>
                            <FormDescription>
                                {t("themeDescription")}
                            </FormDescription>
                            <FormMessage />
                            <RadioGroup
                                onValueChange={field.onChange}
                                value={field.value}
                                className="grid max-w-4xl grid-cols-1 gap-8 pt-2 md:grid-cols-3"
                            >
                                <ThemeItem
                                    value="system"
                                    label={t("themeSystem")}
                                    previewClassName="hover:border-accent"
                                >
                                    <div className="space-y-2 rounded-sm bg-linear-to-r from-[#ecedef] to-slate-950 p-2">
                                        <div className="space-y-2 rounded-md bg-white/90 p-2 shadow-xs">
                                            <div className="h-2 w-[80px] rounded-lg bg-[#d5d7db]" />
                                            <div className="h-2 w-[100px] rounded-lg bg-[#d5d7db]" />
                                        </div>
                                        <div className="flex items-center space-x-2 rounded-md bg-slate-800/90 p-2 shadow-xs">
                                            <div className="h-4 w-4 rounded-full bg-slate-400" />
                                            <div className="h-2 w-[100px] rounded-lg bg-slate-400" />
                                        </div>
                                        <div className="rounded-md border border-white/30 px-2 py-1 text-center text-xs font-medium text-white">
                                            {t("themeSystem")}
                                        </div>
                                    </div>
                                </ThemeItem>
                                <ThemeItem
                                    value="light"
                                    label={t("themeLight")}
                                    previewClassName="hover:border-accent"
                                >
                                    <div className="space-y-2 rounded-sm bg-[#ecedef] p-2">
                                        <div className="space-y-2 rounded-md bg-white p-2 shadow-xs">
                                            <div className="h-2 w-[80px] rounded-lg bg-[#ecedef]" />
                                            <div className="h-2 w-[100px] rounded-lg bg-[#ecedef]" />
                                        </div>
                                        <div className="flex items-center space-x-2 rounded-md bg-white p-2 shadow-xs">
                                            <div className="h-4 w-4 rounded-full bg-[#ecedef]" />
                                            <div className="h-2 w-[100px] rounded-lg bg-[#ecedef]" />
                                        </div>
                                        <div className="flex items-center space-x-2 rounded-md bg-white p-2 shadow-xs">
                                            <div className="h-4 w-4 rounded-full bg-[#ecedef]" />
                                            <div className="h-2 w-[100px] rounded-lg bg-[#ecedef]" />
                                        </div>
                                    </div>
                                </ThemeItem>
                                <ThemeItem
                                    value="dark"
                                    label={t("themeDark")}
                                    previewClassName="bg-popover hover:bg-accent hover:text-accent-foreground"
                                >
                                    <div className="space-y-2 rounded-sm bg-slate-950 p-2">
                                        <div className="space-y-2 rounded-md bg-slate-800 p-2 shadow-xs">
                                            <div className="h-2 w-[80px] rounded-lg bg-slate-400" />
                                            <div className="h-2 w-[100px] rounded-lg bg-slate-400" />
                                        </div>
                                        <div className="flex items-center space-x-2 rounded-md bg-slate-800 p-2 shadow-xs">
                                            <div className="h-4 w-4 rounded-full bg-slate-400" />
                                            <div className="h-2 w-[100px] rounded-lg bg-slate-400" />
                                        </div>
                                        <div className="flex items-center space-x-2 rounded-md bg-slate-800 p-2 shadow-xs">
                                            <div className="h-4 w-4 rounded-full bg-slate-400" />
                                            <div className="h-2 w-[100px] rounded-lg bg-slate-400" />
                                        </div>
                                    </div>
                                </ThemeItem>
                            </RadioGroup>
                        </FormItem>
                    )}
                />

                <Button type="submit" disabled={isUpdatingPreferences}>
                    {isUpdatingPreferences
                        ? t("submitPending")
                        : t("submitDefault")}
                </Button>
            </form>
        </Form>
    );
}
