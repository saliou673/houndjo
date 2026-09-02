"use client";

import { useTransition } from "react";
import { useRouter } from "next/navigation";
import { useLocale, useTranslations } from "next-intl";
import { useQueryClient } from "@tanstack/react-query";
import {
    getUserDetailsQueryKey,
    useGetUserDetails,
    useUpdateAccount,
} from "@api-client";
import { Check, Languages } from "lucide-react";
import { useSession } from "next-auth/react";
import { handleServerError } from "@/lib/handle-server-error";
import { cn } from "@/lib/utils";
import { setLocale } from "@/i18n/actions";
import { locales, type Locale } from "@/i18n/locales";
import { Button } from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export function LanguageSwitch() {
    const locale = useLocale();
    const t = useTranslations("LanguageSwitch");
    const router = useRouter();
    const queryClient = useQueryClient();
    const { status } = useSession();
    const { data: user } = useGetUserDetails(undefined, {
        query: { enabled: status === "authenticated" },
    });
    const { mutateAsync: updateAccount } = useUpdateAccount({
        mutation: { onError: handleServerError },
    });
    const [isPending, startTransition] = useTransition();

    const localeLabels: Record<Locale, string> = {
        en: t("english"),
        fr: t("french"),
    };

    const handleSetLocale = (nextLocale: Locale) => {
        startTransition(async () => {
            await setLocale(nextLocale);

            if (status === "authenticated" && user) {
                try {
                    const updatedUser = await updateAccount({
                        data: {
                            firstName: user.firstName,
                            lastName: user.lastName,
                            phoneNumber: user.phoneNumber,
                            birthDate: user.birthDate,
                            gender: user.gender,
                            address: user.address,
                            languageKey: nextLocale,
                            imageUrl: user.imageUrl,
                        },
                    });
                    queryClient.setQueryData(
                        getUserDetailsQueryKey(),
                        updatedUser
                    );
                } catch {
                    // Error already reported via handleServerError.
                }
            }

            router.refresh();
        });
    };

    return (
        <DropdownMenu modal={false}>
            <DropdownMenuTrigger asChild>
                <Button
                    variant="ghost"
                    size="icon"
                    className="scale-95 rounded-full"
                    disabled={isPending}
                >
                    <Languages className="size-[1.2rem]" />
                    <span className="sr-only">{t("toggleLabel")}</span>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                {locales.map((item) => (
                    <DropdownMenuItem
                        key={item}
                        onSelect={() => handleSetLocale(item)}
                    >
                        {localeLabels[item]}
                        <Check
                            size={14}
                            className={cn("ms-auto", locale !== item && "hidden")}
                        />
                    </DropdownMenuItem>
                ))}
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
