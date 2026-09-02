"use client";

import { useQueryClient } from "@tanstack/react-query";
import {
    displayPreferencesTextSizeEnum,
    getCurrentUserPreferencesQueryKey,
    getUserDetailsQueryKey,
    useGetCurrentUserPreferences,
    useUpdateCurrentUserPreferences,
    type DisplayPreferencesTextSizeEnumKey,
    type UserSummary,
} from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";

const TEXT_SIZES: DisplayPreferencesTextSizeEnumKey[] = [
    displayPreferencesTextSizeEnum.SMALL,
    displayPreferencesTextSizeEnum.DEFAULT,
    displayPreferencesTextSizeEnum.LARGE,
];

// Not the sidebar-item visibility checkboxes this used to ship with
// (recents/home/desktop/...) - those describe a desktop sidebar layout with
// no equivalent on the mobile app this settings section also has to work
// on. Text size and reduced motion are meaningful everywhere.
export function DisplayForm() {
    const t = useTranslations("SettingsDisplay.form");
    const queryClient = useQueryClient();

    const {
        data: preferences,
        isLoading,
        isError,
    } = useGetCurrentUserPreferences();

    const { mutate: updatePreferences, isPending } =
        useUpdateCurrentUserPreferences({
            mutation: {
                onSuccess: (updatedPreferences) => {
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

    function onTextSizeChange(textSize: DisplayPreferencesTextSizeEnumKey) {
        if (!preferences) return;

        updatePreferences({
            data: {
                appearance: preferences.appearance,
                notifications: preferences.notifications,
                display: { ...preferences.display, textSize },
            },
        });
    }

    function onReduceMotionChange(reduceMotion: boolean) {
        if (!preferences) return;

        updatePreferences({
            data: {
                appearance: preferences.appearance,
                notifications: preferences.notifications,
                display: { ...preferences.display, reduceMotion },
            },
        });
    }

    if (isLoading) {
        return <Skeleton className="h-48 w-full" />;
    }

    if (isError || !preferences) {
        return (
            <p className="text-sm text-muted-foreground">{t("loadError")}</p>
        );
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>{t("textSizeLabel")}</CardTitle>
                <p className="text-sm text-muted-foreground">
                    {t("textSizeDescription")}
                </p>
            </CardHeader>
            <CardContent className="space-y-6">
                <RadioGroup
                    value={preferences.display.textSize}
                    onValueChange={(value) =>
                        onTextSizeChange(
                            value as DisplayPreferencesTextSizeEnumKey
                        )
                    }
                    disabled={isPending}
                    className="flex flex-col gap-2"
                >
                    {TEXT_SIZES.map((size) => (
                        <div key={size} className="flex items-center gap-2">
                            <RadioGroupItem value={size} id={`text-size-${size}`} />
                            <Label htmlFor={`text-size-${size}`}>
                                {t(`textSize${size}`)}
                            </Label>
                        </div>
                    ))}
                </RadioGroup>

                <div className="flex flex-row items-center justify-between rounded-lg border p-4">
                    <div className="space-y-0.5">
                        <Label htmlFor="reduce-motion-toggle" className="text-base">
                            {t("reduceMotionLabel")}
                        </Label>
                        <p className="text-sm text-muted-foreground">
                            {t("reduceMotionDescription")}
                        </p>
                    </div>
                    <Switch
                        id="reduce-motion-toggle"
                        checked={preferences.display.reduceMotion}
                        disabled={isPending}
                        onCheckedChange={onReduceMotionChange}
                    />
                </div>
            </CardContent>
        </Card>
    );
}
