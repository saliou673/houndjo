"use server";

import { cookies } from "next/headers";
import { type Locale, LOCALE_COOKIE_NAME } from "./locales";

const LOCALE_COOKIE_MAX_AGE = 60 * 60 * 24 * 365; // 1 year

export async function setLocale(locale: Locale) {
    const cookieStore = await cookies();
    cookieStore.set(LOCALE_COOKIE_NAME, locale, {
        maxAge: LOCALE_COOKIE_MAX_AGE,
        path: "/",
    });
}
