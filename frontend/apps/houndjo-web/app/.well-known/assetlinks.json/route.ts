import { NextResponse } from "next/server";

// Must match `BUNDLE_IDENTIFIER` / `android.package` in
// frontend/apps/houndjo-mobile/app.config.ts.
const ANDROID_PACKAGE = "com.houndjo.mobile";

// TODO(epic:ci-release, #71): replace with the real SHA-256 signing
// certificate fingerprint(s) once EAS Build generates (or an existing)
// Android upload/release keystore exists. Until then, Android cannot
// verify this file and universal links (#91, #110) fall back to opening in
// Chrome with a disambiguation prompt instead of the app directly - the
// custom `houndjomobile://` scheme still works regardless. Get the
// fingerprint with:
//   keytool -list -v -keystore <path-to-keystore> | grep 'SHA256:'
const SHA256_CERT_FINGERPRINTS = ["00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00"];

export async function GET() {
    return NextResponse.json(
        [
            {
                relation: ["delegate_permission/common.handle_all_urls"],
                target: {
                    namespace: "android_app",
                    package_name: ANDROID_PACKAGE,
                    sha256_cert_fingerprints: SHA256_CERT_FINGERPRINTS,
                },
            },
        ],
        { headers: { "Content-Type": "application/json" } }
    );
}
