"use client";

import { format, parseISO } from "date-fns";
import { enUS, fr } from "date-fns/locale";
import { useLocale, useTranslations } from "next-intl";
import { type UserSummary, useGetUserDetails } from "@api-client";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

const dateFnsLocales = { en: enUS, fr } as const;

function formatBirthDate(
    birthDate: string | undefined,
    locale: string,
    notProvidedLabel: string
) {
    if (!birthDate) return notProvidedLabel;

    const parsedDate = parseISO(birthDate);
    return Number.isNaN(parsedDate.getTime())
        ? birthDate
        : format(parsedDate, "PPP", {
              locale: dateFnsLocales[locale as keyof typeof dateFnsLocales],
          });
}

type EnumTranslator = (value: string) => string;

function ProfileSummary({
    user,
    t,
    tGender,
    tStatus,
}: {
    user: UserSummary;
    t: ReturnType<typeof useTranslations>;
    tGender: EnumTranslator;
    tStatus: EnumTranslator;
}) {
    const locale = useLocale();
    const notProvided = t("notProvided");

    return (
        <Card>
            <CardHeader>
                <CardTitle>{t("summaryTitle")}</CardTitle>
                <CardDescription>{t("summaryDescription")}</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-1">
                    <p className="text-sm font-medium">
                        {t("fields.email")}
                    </p>
                    <p className="text-sm text-muted-foreground">
                        {user.email}
                    </p>
                </div>
                <div className="space-y-1">
                    <p className="text-sm font-medium">
                        {t("fields.status")}
                    </p>
                    <p className="text-sm text-muted-foreground">
                        {user.status ? tStatus(user.status) : notProvided}
                    </p>
                </div>
                <div className="space-y-1">
                    <p className="text-sm font-medium">
                        {t("fields.firstName")}
                    </p>
                    <p className="text-sm text-muted-foreground">
                        {user.firstName}
                    </p>
                </div>
                <div className="space-y-1">
                    <p className="text-sm font-medium">
                        {t("fields.lastName")}
                    </p>
                    <p className="text-sm text-muted-foreground">
                        {user.lastName}
                    </p>
                </div>
                <div className="space-y-1">
                    <p className="text-sm font-medium">
                        {t("fields.birthDate")}
                    </p>
                    <p className="text-sm text-muted-foreground">
                        {formatBirthDate(user.birthDate, locale, notProvided)}
                    </p>
                </div>
                <div className="space-y-1">
                    <p className="text-sm font-medium">
                        {t("fields.gender")}
                    </p>
                    <p className="text-sm text-muted-foreground">
                        {user.gender ? tGender(user.gender) : notProvided}
                    </p>
                </div>
                <div className="space-y-1">
                    <p className="text-sm font-medium">
                        {t("fields.phoneNumber")}
                    </p>
                    <p className="text-sm text-muted-foreground">
                        {user.phoneNumber || notProvided}
                    </p>
                </div>
                <div className="space-y-1">
                    <p className="text-sm font-medium">
                        {t("fields.language")}
                    </p>
                    <p className="text-sm text-muted-foreground">
                        {user.languageKey || notProvided}
                    </p>
                </div>
                <div className="space-y-1 sm:col-span-2">
                    <p className="text-sm font-medium">
                        {t("fields.address")}
                    </p>
                    <p className="text-sm text-muted-foreground">
                        {user.address || notProvided}
                    </p>
                </div>
            </CardContent>
        </Card>
    );
}

function ProfileFormSkeleton() {
    return <Skeleton className="h-72 w-full" />;
}

export function ProfileForm() {
    const { data: user, isLoading, isError } = useGetUserDetails();
    const t = useTranslations("SettingsProfile");
    const tGender = useTranslations("UserEnums.gender");
    const tStatus = useTranslations("UserEnums.status");

    if (isLoading) {
        return <ProfileFormSkeleton />;
    }

    if (isError || !user) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle>{t("errorTitle")}</CardTitle>
                    <CardDescription>{t("errorDescription")}</CardDescription>
                </CardHeader>
            </Card>
        );
    }

    return (
        <ProfileSummary user={user} t={t} tGender={tGender} tStatus={tStatus} />
    );
}
