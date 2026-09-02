# Spring Next SaaS Starter

A production-oriented starter kit for building SaaS products, combining a **Spring Boot** backend built with
**Hexagonal Architecture** and a **Next.js** web app, sharing a fully-typed API client generated from the backend's
OpenAPI spec. A React Native (Expo) mobile scaffold is included and wired to the same client.

The goal is to give you authentication, RBAC, admin tooling, and the cross-stack plumbing already solved, so you can
focus on your product's actual domain from day one.

## Screenshots

| Landing page | Sign in |
|---|---|
| ![Landing page](./docs/screenshots/landing.jpg) | ![Sign in](./docs/screenshots/sign-in.jpg) |

| Dashboard | Users |
|---|---|
| ![Dashboard](./docs/screenshots/dashboard.jpg) | ![Users](./docs/screenshots/users.jpg) |

| Role groups | Appearance settings |
|---|---|
| ![Role groups](./docs/screenshots/role-groups.jpg) | ![Appearance settings](./docs/screenshots/appearance-settings.jpg) |

### Mobile app

<p>
  <img src="./docs/screenshots/mobile-sign-in.jpg" alt="Mobile sign in" width="220" />
  <img src="./docs/screenshots/mobile-home.jpg" alt="Mobile home" width="220" />
  <img src="./docs/screenshots/mobile-settings.jpg" alt="Mobile settings" width="220" />
</p>

## Table of contents

- [Screenshots](#screenshots)
- [Features](#features)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting started](#getting-started)
- [Environment variables](#environment-variables)
- [Running the project](#running-the-project)
- [API documentation](#api-documentation)
- [Testing](#testing)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [License](#license)

## Features

### Authentication & account lifecycle

- Email/password registration with account activation by email
- JWT authentication with refresh-token rotation and a "remember me" long-lived session
- Login / logout, forgot password, and account recovery flows
- Two-factor authentication (TOTP): setup, confirmation, verification at login, and disable
- Email change with confirmation step, password change, soft-delete of own account
- User invitation flow for admin-managed accounts (invite by email, time-boxed invitation codes)
- Configurable retention/cleanup jobs for non-activated and soft-deleted accounts

### RBAC (Role-Based Access Control)

- Role groups (Sysadmin, Admin, User, Anonymous seeded by default) composed of fine-grained permissions
- Permission codes follow a `resource:action` / `resource:action:own` convention, enforced with
  `@PreAuthorize("hasAuthority('permission:code')")`
- Admin endpoints to manage role groups, assign/revoke permissions, and inspect a user's effective permissions
- See [docs/permissions.md](./docs/permissions.md) for the full permission matrix

### Admin & platform tooling

- User administration (list, update, deactivate, assign role groups)
- Runtime application configuration by category/code (e.g. supported currencies, payment modes), with public
  read-only and admin-managed endpoints
- Security settings administration
- Contact form endpoint with configurable notification recipients
- Rate limiting (Resilience4j) on auth and general API endpoints, independently configurable
- API versioning via the `X-API-Version` header
- Health checks (Spring Actuator) and Prometheus metrics (Micrometer)

### Internationalization (i18n)

The whole stack is internationalized (English/French out of the box):

- **Backend** — domain exceptions, email templates, and API error messages are all localized. Locale is resolved
  per-request from the `Accept-Language` header, or from the authenticated user's stored language preference when
  connected (which takes precedence)
- **Frontend** — the admin dashboard UI is fully internationalized via [next-intl](https://next-intl.dev/), with a
  language switcher in the header that persists the chosen language to the user's account, so it's remembered across
  sessions and devices

### Frontend (web)

A [shadcn/ui](https://ui.shadcn.com/)-based admin dashboard (built on top of the
[shadcn-admin](https://github.com/satnaing/shadcn-admin) template) already wired to the backend:

- Auth pages: sign in, sign up, OTP verification, forgot/recover password, invitation completion
- Dashboard, user management, role groups, and app configurations
- Account settings: profile, notifications, appearance, display
- Legal/informational pages: terms, privacy, cookie policy, help center, contact
- Fully typed data layer via TanStack Query hooks generated from the backend OpenAPI spec — no hand-written HTTP
  calls

### Mobile (early scaffold)

`apps/houndjo-mobile` is an Expo/React Native app set up to consume the same shared API client. It currently ships
with the default Expo Router scaffold — a starting point to build native screens on, not a built-out app yet.

### Wired in, ready to build on

The following dependencies are already in the backend's `pom.xml` and partially reflected in configuration, but not
yet exposed through a full feature — they exist so you don't have to add the plumbing yourself:

- **Stripe** SDK (`stripe-java`, `@stripe/stripe-js`) and a `PAYMENT_MODE` app-configuration category
- **AWS SDK** (for object storage, e.g. S3-backed file uploads)
- **Apache PDFBox** plus branding assets under `backend/houndjo-restapi/houndjo-app/src/main/resources/billing/`
  (fonts, logo) — intended for invoice/receipt PDF generation

## Tech stack

| Layer                | Technology                                                                          |
|-----------------------|--------------------------------------------------------------------------------------|
| Backend               | Java 25, Spring Boot 4, Hexagonal Architecture (Ports & Adapters)                    |
| Database              | PostgreSQL, Liquibase migrations                                                      |
| Backend security      | Spring Security, JWT + refresh-token rotation, TOTP 2FA, Resilience4j rate limiting   |
| API docs              | springdoc-openapi (Swagger UI + OpenAPI 3.1 JSON)                                     |
| Observability         | Spring Actuator, Micrometer + Prometheus                                              |
| Backend testing       | JUnit 5, Testcontainers (integration tests against a real PostgreSQL container)       |
| Frontend monorepo     | pnpm workspace (`frontend/`)                                                          |
| Web app               | Next.js 16, React 19, TypeScript, Tailwind CSS 4, shadcn/ui (Radix primitives)         |
| Web data layer        | TanStack Query, TanStack Table, Zustand, React Hook Form + Zod                        |
| Internationalization  | Backend: Spring `MessageSource` (English/French) — Frontend: next-intl (English/French) |
| API client codegen    | [Kubb](https://www.kubb.dev/) — generates a typed TanStack Query client from the backend's OpenAPI spec |
| Mobile app            | Expo + React Native (scaffold)                                                        |
| Local email testing   | [MailDev](https://github.com/maildev/maildev)                                         |
| Payments (wired in)   | Stripe                                                                                 |

## Project structure

```
houndjo/
├── backend/
│   └── houndjo-restapi/            # Spring Boot Maven multi-module project
│       ├── houndjo-core/           # Domain layer: entities, ports, no framework dependencies
│       ├── houndjo-app/            # Application + infrastructure layers: use cases, REST controllers,
│       │                           # persistence adapters, security, config
│       └── houndjo-tests/          # Integration tests (Testcontainers)
├── frontend/                       # pnpm workspace root
│   ├── apps/
│   │   ├── houndjo-web/            # Next.js web application
│   │   └── houndjo-mobile/         # Expo / React Native application
│   └── packages/
│       └── houndjo-apiclient/      # Kubb-generated TanStack Query client (shared by web & mobile)
├── docker/
│   └── docker-compose.yml          # PostgreSQL + MailDev for local development
└── docs/                           # Architecture, conventions, permissions, SMTP setup
```

The backend follows **Hexagonal Architecture**:

- **Domain** (`houndjo-core/.../domain`) — entities, value objects, and port interfaces. Zero framework
  dependencies; testable in complete isolation.
- **Application** (`houndjo-app/.../application`) — use-case orchestrators depending only on domain ports.
- **Infrastructure** (`houndjo-app/.../infrastructure`) — adapters implementing domain ports: REST controllers,
  JPA persistence, email/notification senders, 2FA providers, security configuration.

See [docs/architecture.md](./docs/architecture.md) for diagrams.

## Prerequisites

- [SDKMAN!](https://sdkman.io/install) to install Java and Maven
  - `sdk install java 21.0.8-tem` (the backend targets Java 25 — use the latest LTS/GA available to your SDKMAN
    candidates list, or adjust `java.version` in the root `pom.xml` to match your installed JDK)
  - `sdk install maven 3.9.9`
- [nvm](https://github.com/nvm-sh/nvm) to install Node.js (Node 20+)
- [pnpm](https://pnpm.io/installation)
- [Docker](https://docs.docker.com/get-docker/) and Docker Compose, to run PostgreSQL and MailDev locally

## Getting started

### 1. Create your project

The quickest way to start a new project from this template is the scaffold script: it clones the repo and renames
every `houndjo` reference (Java package, Maven artifacts, pnpm packages, Docker/DB names, mobile bundle id, docs, ...)
to your project's name.

```bash
curl -fsSL https://raw.githubusercontent.com/saliou673/spring-next-saas-starter/main/scripts/create-project.sh | bash
```

It will prompt you for a project name (kebab-case, e.g. `acme-billing`) and a target directory, then clone and rename
in place. Run `curl -fsSL .../create-project.sh | bash -s -- --help` to see non-interactive flags.

Maintaining your own fork? Pass `--repo <your-fork-url>` (or set `SAAS_STARTER_REPO`) so the script scaffolds from
your fork instead of upstream — see the comment at the top of [`create-project.sh`](./scripts/create-project.sh) for
how to make that the default for your users.

Prefer to do it by hand instead?

```bash
git clone git@github.com:saliou673/spring-next-saas-starter.git
cd spring-next-saas-starter
```

### 2. Start local infrastructure (PostgreSQL + MailDev)

```bash
docker compose -f docker/docker-compose.yml up -d
```

This starts:

- PostgreSQL on `localhost:5432` (db `houndjodb`, user/password `admin`/`admin` — override via the backend `.env`)
- MailDev on `localhost:1025` (SMTP) and `localhost:1080` (web UI to inspect sent emails)

Stop it with:

```bash
docker compose -f docker/docker-compose.yml down
```

### 3. Configure the backend

```bash
cp backend/houndjo-restapi/.env.example backend/houndjo-restapi/.env
```

Fill in `backend/houndjo-restapi/.env` — see [Environment variables](#environment-variables) below.

### 4. Install and run the backend

```bash
cd backend/houndjo-restapi
mvn clean install
mvn -pl houndjo-app spring-boot:run
```

The API starts on `http://localhost:8080`.

### 5. Install frontend dependencies and generate the API client

```bash
cd frontend
pnpm install
pnpm generate:api   # backend must be running and serving OpenAPI docs at /api/docs
```

### 6. Run the web app

```bash
pnpm web   # from frontend/, or `pnpm dev` from frontend/apps/houndjo-web
```

The web app starts on `http://localhost:3000`.

### 7. (Optional) Run the mobile app

```bash
pnpm --filter houndjo-mobile start
```

## Environment variables

All backend configuration lives in `backend/houndjo-restapi/.env` (see
[`.env.example`](./backend/houndjo-restapi/.env.example) for the full, commented list). Key groups:

- **Database** — `POSTGRESQL_HOST` / `_PORT` / `_DATABASE_NAME` / `_USERNAME` / `_PASSWORD`
- **JWT** — `JWT_SECRET`, `DEFAULT_TOKEN_VALIDITY_IN_SECONDS`, `TOKEN_VALIDITY_IN_SECONDS_FOR_REMEMBER_ME`
- **SMTP / email** — see [docs/how-to-configure-smtp.md](./docs/how-to-configure-smtp.md) for MailDev vs Gmail setup
- **Account lifecycle** — activation/reset code validity, invitation code length/validity, cleanup cron
- **2FA** — `TWO_FACTOR_CODE_VALIDITY_PERIOD`, `TWO_FACTOR_CODE_LENGTH`, `TOTP_ISSUER`
- **Rate limiting** — `RATE_LIMIT_ENABLED` and per-scope limit/refresh-period pairs for auth vs. general API
  endpoints
- **Contact form** — `CONTACT_FORM_RECIPIENT_EMAILS`
- **CORS** — `CORS_ALLOWED_ORIGINS`
- **Payments** — `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`

Never commit a filled-in `.env` file — it's already covered by `.gitignore`.

## Running the project

| Component      | Command                                                          | URL                          |
|-----------------|-------------------------------------------------------------------|-------------------------------|
| Infrastructure  | `docker compose -f docker/docker-compose.yml up -d`               | —                              |
| Backend API     | `mvn -pl houndjo-app spring-boot:run` (from `backend/houndjo-restapi`) | http://localhost:8080          |
| Web app         | `pnpm web` (from `frontend/`)                                      | http://localhost:3000          |
| Mobile app      | `pnpm --filter houndjo-mobile start`                                | Expo Go / simulator            |
| MailDev UI      | started via Docker Compose above                                   | http://localhost:1080          |

## API documentation

- Swagger UI: http://localhost:8080/api/swagger-ui
- OpenAPI JSON: http://localhost:8080/api/docs
- Actuator health: http://localhost:8080/actuator/health

The OpenAPI spec at `/api/docs` is the source of truth for `frontend/packages/houndjo-apiclient`. Whenever a backend
endpoint changes, re-run `pnpm generate:api` and commit the regenerated client before updating any consuming
frontend code.

## Testing

Backend integration tests spin up a real PostgreSQL instance via Testcontainers (Docker must be running):

```bash
cd backend/houndjo-restapi
mvn test
```

Frontend checks:

```bash
cd frontend
pnpm lint
pnpm format:check
pnpm build
```

## Documentation

- [Architecture](./docs/architecture.md)
- [Coding conventions](./docs/coding-convention.md) (naming, commit format, branch naming, PR guidelines)
- [Permissions & role groups](./docs/permissions.md)
- [How to configure SMTP](./docs/how-to-configure-smtp.md)

## Contributing

Contributions are welcome — bug fixes, new features, and documentation improvements alike.

1. Fork the repository and create a branch from `main` following the naming convention in
   [docs/coding-convention.md](./docs/coding-convention.md#branch-naming) (e.g. `feature/123-add-webhook-support`,
   `bugfix/124-fix-token-refresh`).
2. For non-trivial changes, open an issue or discussion first to align on the approach before writing code.
3. Follow the project's [coding conventions](./docs/coding-convention.md) — naming, package/module layering
   (respect the hexagonal boundaries: domain code must not depend on Spring or infrastructure), and commit message
   format (`type(scope): subject`, e.g. `feat(auth): add JWT token refresh mechanism`).
4. Make sure the relevant checks pass locally before opening a PR:
   - Backend: `mvn test` (from `backend/houndjo-restapi`)
   - Frontend: `pnpm lint && pnpm format:check && pnpm build` (from `frontend/`)
5. Open a pull request against `main` with a clear description of the change, link the related issue, and add
   screenshots for UI changes.

Please don't commit secrets, `.env` files, or generated build artifacts.

## License

This project is licensed under the [MIT License](./LICENSE).