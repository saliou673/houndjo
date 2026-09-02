"use client";

import { useQueryClient } from "@tanstack/react-query";
import {
    getCurrentUserPreferencesQueryKey,
    getUserDetailsQueryKey,
    useGetCurrentUserPreferences,
    useUpdateCurrentUserPreferences,
    type UserSummary,
} from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";

// There's no `notifications` field on UserPreferences beyond
// `productUpdatesEnabled` - security-relevant emails are always sent and
// aren't a user preference, and no other notification type exists in this
// app to make a preference meaningful for.
export function NotificationsForm() {
    const t = useTranslations("SettingsNotifications.form");
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

    function onToggle(productUpdatesEnabled: boolean) {
        if (!preferences) return;

        updatePreferences({
            data: {
                appearance: preferences.appearance,
                notifications: { productUpdatesEnabled },
                display: preferences.display,
            },
        });
    }

    if (isLoading) {
        return <Skeleton className="h-24 w-full" />;
    }

    if (isError || !preferences) {
        return (
            <p className="text-sm text-muted-foreground">{t("loadError")}</p>
        );
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>{t("emailNotificationsTitle")}</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="flex flex-row items-center justify-between rounded-lg border p-4">
                    <div className="space-y-0.5">
                        <Label htmlFor="product-updates-toggle" className="text-base">
                            {t("productUpdatesLabel")}
                        </Label>
                        <p className="text-sm text-muted-foreground">
                            {t("productUpdatesDescription")}
                        </p>
                    </div>
                    <Switch
                        id="product-updates-toggle"
                        checked={preferences.notifications.productUpdatesEnabled}
                        disabled={isPending}
                        onCheckedChange={onToggle}
                    />
                </div>
                <div className="mt-4 flex flex-row items-center justify-between rounded-lg border p-4 opacity-70">
                    <div className="space-y-0.5">
                        <Label className="text-base">
                            {t("securityLabel")}
                        </Label>
                        <p className="text-sm text-muted-foreground">
                            {t("securityDescription")}
                        </p>
                    </div>
                    <Switch checked disabled aria-readonly />
                </div>
            </CardContent>
        </Card>
    );
}
