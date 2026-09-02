import { useTranslations } from "next-intl";
import {
    userDetailsGenderEnum,
    userDetailsStatusEnum,
    type UserDetailsGenderEnumKey,
    type UserDetailsStatusEnumKey,
} from "@api-client";
import { type UserGender, type UserStatus } from "./schema";

export const userStatusBadgeClassNames: Record<UserStatus, string> = {
    [userDetailsStatusEnum.NOT_ACTIVATED]:
        "bg-sky-200/40 text-sky-900 border-sky-300 dark:text-sky-100",
    [userDetailsStatusEnum.ACTIVATED]:
        "bg-teal-100/30 text-teal-900 border-teal-200 dark:text-teal-200",
    [userDetailsStatusEnum.DEACTIVATED]: "bg-neutral-300/40 border-neutral-300",
    [userDetailsStatusEnum.LOCKED]:
        "bg-amber-100/50 text-amber-900 border-amber-300 dark:text-amber-200",
    [userDetailsStatusEnum.BANNED]:
        "bg-destructive/10 text-destructive border-destructive/20 dark:bg-destructive/50 dark:text-primary",
};

export function useUserStatusLabels(): Record<UserStatus, string> {
    const t = useTranslations("UserEnums.status");

    return {
        [userDetailsStatusEnum.NOT_ACTIVATED]: t("NOT_ACTIVATED"),
        [userDetailsStatusEnum.ACTIVATED]: t("ACTIVATED"),
        [userDetailsStatusEnum.DEACTIVATED]: t("DEACTIVATED"),
        [userDetailsStatusEnum.LOCKED]: t("LOCKED"),
        [userDetailsStatusEnum.BANNED]: t("BANNED"),
    };
}

export function useUserStatusOptions(): {
    label: string;
    value: UserDetailsStatusEnumKey;
}[] {
    const labels = useUserStatusLabels();

    return Object.values(userDetailsStatusEnum).map((value) => ({
        label: labels[value],
        value,
    }));
}

export function useGenderLabels(): Record<UserGender, string> {
    const t = useTranslations("UserEnums.gender");

    return {
        [userDetailsGenderEnum.MALE]: t("MALE"),
        [userDetailsGenderEnum.FEMALE]: t("FEMALE"),
    };
}

export function useGenderOptions(): {
    label: string;
    value: UserDetailsGenderEnumKey;
}[] {
    const labels = useGenderLabels();

    return Object.values(userDetailsGenderEnum).map((value) => ({
        label: labels[value],
        value,
    }));
}
