import type { NextConfig } from "next";
import path from "node:path";
import createNextIntlPlugin from "next-intl/plugin";

const withNextIntl = createNextIntlPlugin("./src/i18n/request.ts");

const nextConfig: NextConfig = {
    reactStrictMode: true,
    outputFileTracingRoot: path.join(__dirname),
    transpilePackages: ["houndjo-apiclient"],
    turbopack: {
        root: __dirname,
    },
};

export default withNextIntl(nextConfig);
