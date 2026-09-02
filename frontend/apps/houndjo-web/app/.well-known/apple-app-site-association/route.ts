import { NextResponse } from "next/server";

// Must match `BUNDLE_IDENTIFIER` in frontend/apps/houndjo-mobile/app.config.ts.
const BUNDLE_IDENTIFIER = "com.houndjo.mobile";

// TODO(epic:ci-release, #71): replace with the real Apple Developer Team ID
// once EAS Build / an Apple Developer account is set up. Until then, iOS
// cannot verify this file and universal links (#91, #110) fall back to
// opening in Safari instead of the app - the custom `houndjomobile://`
// scheme still works regardless.
const APPLE_TEAM_ID = "TEAMID";

// Paths houndjo-mobile registers via `ios.associatedDomains` (see its
// app.config.ts `AUTH_LINK_PATHS`). iOS fetches this file over HTTPS from
// the app's associated domain to decide whether to open the app instead of
// Safari for a matching link.
const PATHS = ["/reset-password", "/account/invitation", "/activate"];

export async function GET() {
    return NextResponse.json(
        {
            applinks: {
                apps: [],
                details: [
                    {
                        appID: `${APPLE_TEAM_ID}.${BUNDLE_IDENTIFIER}`,
                        paths: PATHS,
                    },
                ],
            },
        },
        { headers: { "Content-Type": "application/json" } }
    );
}
