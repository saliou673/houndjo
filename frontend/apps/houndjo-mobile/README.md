# Houndjo Mobile

Expo / React Native app for Houndjo, built with [file-based routing](https://docs.expo.dev/router/introduction)
(`expo-router`). It talks to the same backend as [`houndjo-web`](../houndjo-web) through the shared, generated
[`houndjo-apiclient`](../../packages/houndjo-apiclient) package — no hand-written HTTP calls.

## Prerequisites

- Node.js 20+ and [pnpm](https://pnpm.io/installation) (this app lives in the `frontend/` pnpm workspace, alongside
  `houndjo-web`)
- The backend running and reachable — see the root [README](../../../README.md#getting-started) for how to start
  PostgreSQL/MailDev and the Spring Boot API
- To run on a device/simulator: [Expo Go](https://expo.dev/go) for the fastest loop, or a
  [development build](https://docs.expo.dev/develop/development-builds/introduction/) for anything this app uses
  that Expo Go doesn't support (see `expo-dev-client` in `package.json`)
- Xcode (iOS simulator) and/or Android Studio (Android emulator), if you want native simulators instead of Expo Go

## Setup

From the repository root:

```bash
cd frontend
pnpm install
```

```bash
cd apps/houndjo-mobile
cp .env.example .env
```

`.env` isn't required to start the app — every value has a sensible default — but you'll usually want to at least
confirm `EXPO_PUBLIC_API_BASE_URL` points at your backend:

| Variable                    | Purpose                                                                              | Default                                              |
| ---------------------------- | ------------------------------------------------------------------------------------- | ----------------------------------------------------- |
| `APP_ENV`                    | `development` \| `staging` \| `production` — selects the default API/web-app origins in `app.config.ts`. EAS build profiles set this automatically (see [EAS Build](#eas-build)) | `development`                                          |
| `EXPO_PUBLIC_API_BASE_URL`    | Overrides the environment's default backend URL                                       | `http://localhost:8080` in development                |

Then start the app:

```bash
pnpm start
```

This opens the Expo CLI, from which you can launch an iOS simulator, Android emulator, or scan the QR code with
Expo Go. Platform-specific shortcuts:

```bash
pnpm ios       # expo run:ios
pnpm android   # expo run:android
pnpm web       # expo start --web
```

> On a physical device, `localhost` in the API base URL resolves to the phone, not your dev machine.
> `src/constants/env.ts` already rewrites it to the address Expo's dev server was reached on
> (`Constants.expoConfig.hostUri`), so this only matters if you're hitting a non-local backend.

## Available scripts

| Script               | What it does                                                                 |
| --------------------- | ------------------------------------------------------------------------------ |
| `pnpm start`          | Starts the Expo dev server                                                    |
| `pnpm ios` / `android` | Builds and runs a native dev client on a simulator/emulator                   |
| `pnpm web`            | Runs the app in a browser via `react-native-web`                              |
| `pnpm lint`           | `expo lint` — ESLint with `eslint-config-expo`                                |
| `pnpm typecheck`      | `tsc --noEmit`                                                                |
| `pnpm build`          | `expo export` — bundles all platforms; used by CI as a compile/bundle smoke test, **not** a native binary (see [EAS Build](#eas-build) for that) |
| `pnpm reset-project`  | Expo's own scaffold-reset script (not used day-to-day in this app)            |

## CI

[`.github/workflows/ci.yml`](../../../.github/workflows/ci.yml) at the repo root runs `lint`, `typecheck`, and
`build` for this app on every push/PR to `main`, alongside a `format` job (Prettier, whole `frontend/` workspace)
and a `web` job for `houndjo-webapp`.

## EAS Build

[`eas.json`](./eas.json) defines two build profiles:

- **`development`** — internal distribution, includes the dev client (`expo-dev-client`), points at
  `APP_ENV=development`
- **`preview`** — internal distribution, standalone (no dev client), points at `APP_ENV=staging`

Actually running a build (`eas build --profile development`) requires the project to be linked to a real
[EAS](https://expo.dev/eas) account first (`eas init`), which hasn't been done yet.

## Project structure

```
src/
├── app/              # expo-router routes — file-based, mirrors the screens below
│   ├── (auth)/        # Sign-in, sign-up, password reset, etc. — shown when signed out
│   └── (app)/         # Everything else — shown when signed in
├── components/       # Shared UI building blocks (ThemedText, SettingsCard, ErrorScreen, ...)
├── context/           # Theme and text-size providers
├── hooks/             # useAuth, useTheme, useIsOnline, ...
├── i18n/              # react-i18next setup + en/fr locale files
└── lib/               # Auth interceptor, network status, API error helpers
```

## Feature parity vs. the web app

Tracked epic-by-epic against `frontend/apps/houndjo-web`. ✅ done, 🔶 partial, ⬜ not started.

| Area                        | Web reference                          | Status                                                                                                                                                                        |
| ----------------------------- | ----------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Foundation                  | shared API client, auth plumbing          | ✅ Env-based API base URL, shared API client wired in, secure token storage, auth HTTP interceptor with refresh-token rotation, authenticated/unauthenticated navigation shell, theme provider, i18n (en/fr), global API error handling, app branding, Customize tab (branding/theme example screen for forks, replacing the Expo starter's Explore tab) |
| Authentication              | `app/sign-in`, `/sign-up`, etc.            | ✅ Sign-in, remember-me, sign-up, account activation deep link, 2FA challenge at login, forgot/reset password, complete-invitation, logout, deep-link routing for auth email links |
| Account & settings          | `app/settings/*`                           | ✅ Settings navigation shell, profile, change email (with confirmation), change password, 2FA setup/disable, soft-delete own account, appearance (theme), notifications preferences, display preferences, language switcher |
| Dashboard                   | dashboard/home screen                      | ⬜ Not started                                                                                                                                                                     |
| Admin — user management     | `app/users`                                | ✅ Users list, user detail, invite/create user, edit user, deactivate user, assign role group to user, permission-gated admin navigation |
| Admin — role groups         | `app/role-groups`                          | ✅ Role groups list, detail/edit, create, assign/revoke permissions in a role group                                                       |
| Admin — app configurations   | `app/configurations/*`                     | 🔶 Configurations navigation shell and reference-data screen done; file storage, tax rates, and company profile screens are blocked — the web pages they'd mirror were removed (dead links to client components/features that were never built, and have no backend support yet). Security settings screen done. |
| Legal & support pages       | `app/terms`, `/privacy`, `/cookie-policy`, `/contact`, `/help-center` | ✅ All five screens, reachable from Settings while signed in (not pre-auth like web — see PR for #57)                                    |
| Error & edge-case screens   | `app/errors/*`                             | ✅ Forbidden (403, wired to query-level permission errors), server error (500), maintenance (503), not-found (404, wired as expo-router's catch-all), unauthorized/session-expired (401, wired to forced logout), offline/network-state handling |
| CI & release readiness      | —                                           | ✅ CI pipeline (lint/typecheck/build), EAS Build configuration, this README                                                                |

## Learn more

- [Expo documentation](https://docs.expo.dev/)
- [Expo Router](https://docs.expo.dev/router/introduction/)
- Root [README](../../../README.md) — backend setup, environment variables, project-wide tech stack
