import "@/styles/index.css";
import type { Metadata } from "next";
import { NextIntlClientProvider } from "next-intl";
import { getLocale, getMessages } from "next-intl/server";
import { Providers } from "./providers";

export const metadata: Metadata = {
    title: {
        default: "Houndjo",
        template: "%s | Houndjo",
    },
    description: "Houndjo dashboard",
};

type RootLayoutProps = Readonly<{
    children: React.ReactNode;
}>;

export default async function RootLayout({ children }: RootLayoutProps) {
    const locale = await getLocale();
    const messages = await getMessages();

    return (
        <html lang={locale} suppressHydrationWarning>
            <head>
                {/* Prevent FOUC: read cookie and apply theme class before React hydrates */}
                <script
                    dangerouslySetInnerHTML={{
                        __html: `(function(){try{var m=/vite-ui-theme=([^;]+)/.exec(document.cookie);var t=m?decodeURIComponent(m[1]):'system';if(t==='system')t=window.matchMedia('(prefers-color-scheme: dark)').matches?'dark':'light';document.documentElement.classList.add(t);}catch(e){}})()`,
                    }}
                />
            </head>
            <body>
                <NextIntlClientProvider locale={locale} messages={messages}>
                    <Providers>{children}</Providers>
                </NextIntlClientProvider>
            </body>
        </html>
    );
}
