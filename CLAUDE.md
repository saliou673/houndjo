<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the current plan
<!-- SPECKIT END -->

## Product context — Houndjo: SaaS for Quranic schools

Houndjo tracks operations for Quranic schools (madrasas): student memorization/reading progress
across three distinct tracking flows (Sabak = new lesson, Sabqi = recent review, Dhor = long-term
revision), classes/courses, attendance, and manual tuition billing. It is built on the
`spring-next-saas-starter` base: Spring Boot 4 / Java 25 hexagonal backend, Next.js 16 web,
Expo/React Native mobile, generated Kubb API client. Package root: `com.houndjo`.

The full, agent-ready backlog (detailed schemas, endpoints, DTOs, test cases) lives in
`backlog-agent-ready.md` (French) and has been converted 1:1 into GitHub issues — one issue per
ticket, grouped by epic via GitHub **milestones**, labeled `epic:<E-code>` and
`type:<backend|frontend-web|frontend-mobile|fullstack|qa|architecture>`, each carrying resolved
`Depends on: #N` links. Read the target issue for full ticket detail before implementing it;
this section only carries the conventions and architecture that don't belong in a single ticket.

### Epics & build order

```
E-M  Mobile UI migration to BNA UI     (prerequisite for any new mobile screen)
E0   Multi-tenant foundations          (prerequisite for the whole domain)
E1   Organization & members
E2   Quran reference data              (parallelizable from day 0, no dependencies)
E3   Academic structure (classes/courses)
E4   Students, enrollments, pace, sessions
E5   Progress tracking                 (the core differentiator)
E6   Attendance
E7   Billing & tuition (manual)
E8   i18n & responsive (cross-cutting)
E9   Dashboards
```

Critical path: `E0-1 → E0-2 → E1-1 → E3-1 → E3-2 → E4-3 → E4-4 → E5-1 → E5-2 → E5-3/E5-4`.
`E-M` (all 7 tickets) must land before any new business mobile screen (blocks E1-5, E3-4, E4-6,
E5-4, E6-3, E7-4). `E2` has no dependencies and feeds E3-2, E4-3, E5-1 — start it immediately
alongside E-M and E0.

### Domain model highlights

- **Multi-tenancy**: every business table carries an `organization_id` discriminant. Tenant
  resolution is an explicit application-level guard (`TenantContext`, package
  `com.houndjo.application.tenant`) — not a Hibernate filter. Every list `PersistencePort` takes
  `organizationId`; every business use case checks it. See ADR at
  `docs/architecture/adr-001-multitenancy.md` (ticket E0-1).
- **Membership** (not the platform RBAC) carries org-scoped business roles: `SCHOOL_OWNER`,
  `SCHOOL_ADMIN`, `TEACHER` (later: `PARENT`, `STUDENT`). Checked via
  `@authz.hasOrgRole('...')` in `@PreAuthorize`.
- **Student** is a pedagogical profile, not a `User` — no login at MVP. It has a reserved,
  unexposed nullable `userId` for future account linking (V1).
- **Progress has three independent flows for Quran courses**: Sabak (new lesson), Sabqi (recent
  review), Dhor (long-term revision, cycle-based). Tracking only Sabak hides fragile
  memorization — Dhor staleness detection (`ProgressAnalysisService`, ticket E5-2) is the
  product's core differentiator, not an afterthought.
- **Quran reference data** (`quran_surah`, `quran_verse`) is global and immutable — no
  `organization_id`. It backs page/juz/hizb/verse conversions used by course scoping (E3-2),
  pace (E4-3) and progress portions (E5-1).
- **Course typology is extensible**: `CourseType` (`QAIDA`, `QURAN`, `BOOK`) with polymorphic,
  type-specific config rather than hardcoded per-type logic.

### Backend — hexagonal structure per aggregate

Every new aggregate `X` creates exactly these files:

```
houndjo-core/.../domain/models/<ctx>/X.java              # aggregate (extends Auditable<Long>)
houndjo-core/.../domain/models/<ctx>/XFilter.java         # if listable/filterable
houndjo-core/.../domain/ports/in/XUseCase.java            # inbound port (use case)
houndjo-core/.../domain/ports/out/persistenceport/XPersistencePort.java
houndjo-core/.../domain/exceptions/XNotFoundException.java (+ others as needed)
houndjo-app/.../application/<ctx>/XService.java           # implements XUseCase, depends on out ports
houndjo-app/.../infrastructure/adapter/out/persistence/<ctx>/XEntity.java
houndjo-app/.../infrastructure/adapter/out/persistence/<ctx>/XMapper.java       # domain<->entity
houndjo-app/.../infrastructure/adapter/out/persistence/<ctx>/XRepository.java   # Spring Data JPA
houndjo-app/.../infrastructure/adapter/out/persistence/<ctx>/XPersistenceAdapter.java # implements out port
houndjo-app/.../infrastructure/adapter/in/rest/controller/<ctx>/XController.java
houndjo-app/.../infrastructure/adapter/in/rest/controller/dto/<ctx>/*.java      # requests/responses + DTO mappers
houndjo-app/src/main/resources/db/changelog/ddl/NNNNN-x-create-table.sql
houndjo-tests/.../<ctx>/XControllerIT.java                # Testcontainers
```

**Domain rules:**
- Aggregates extend `Auditable<Long>` (provides `id`, `creationDate`, `lastUpdateDate`,
  `lastUpdatedBy`). Never redeclare these fields.
- Private constructor + static factories `create(...)` (new object) and `rehydrate(...)`
  (reconstruction from persistence). See `User`, `RoleGroup`.
- Zero framework dependency in `houndjo-core`. Use `Objects.requireNonNull` for invariants.
- Immutable value objects (records) where relevant (see `Email`, `UserInfo`).

**Persistence adapter rules:** wrap every operation in
`AdapterPersistenceUtils.executeDbOperation(() -> ..., "error message")`. Annotate the adapter
`@Service @Transactional @RequiredArgsConstructor`. See `AppConfigurationPersistenceAdapter`.

**Controller rules:**
- `@RestController @Validated @RequiredArgsConstructor @Tag(name="...")`.
- `@RequestMapping(path="/api/v1/<resource-kebab>", version="1.0")` (versioned via the
  `X-API-Version` header).
- `@PreAuthorize("hasAuthority('<resource>:<action>')")` at class and/or method level.
- Creation → `@ResponseStatus(HttpStatus.CREATED)`; deletion → `NO_CONTENT`.
- Paginated lists → return `PaginatedResult<XDTO>` with
  `@PageableDefault(size=DEFAULT_PAGE_SIZE_INT, sort="creationDate", direction=DESC)`.
- Never expose a JPA entity or a domain model: always DTOs + a DTO mapper.
- Validate requests with `@Valid @RequestBody`, Jakarta constraints (`@NotBlank`, `@NotNull`,
  `@Size`, `@Email`, `@Positive`, ...).

**Liquibase migration rules:**
- Header `--liquibase formatted sql` then `--changeset houndjo:NNNNN-name`.
- `BIGINT GENERATED BY DEFAULT AS IDENTITY` for PKs, `TIMESTAMP NOT NULL DEFAULT now()` for
  audit dates, `last_updated_by VARCHAR(255) NOT NULL DEFAULT 'system'`.
- Named constraints: `pk_<table>`, `uq_<table>_<col>`, `fk_<table>_<ref>`. FKs with an explicit
  `ON DELETE`.
- Add the include to `ddl/master.yml` (and `dml/master.yml` for seeds). Number sequentially
  after the existing changesets (the starter goes up to `00009`).
- Index every frequently filtered column, **especially `organization_id`**.
- **Column naming**: domain fields are `camelCase`, SQL columns are `snake_case`, mapped by the
  `Mapper`. Systematic conversion, e.g.: `organizationId`→`organization_id`,
  `amountPerSession`→`amount_per_session`, `sessionsPerWeek`→`sessions_per_week`,
  `teacherUserId`→`teacher_user_id`, `birthDate`→`birth_date`, `guardianName`→`guardian_name`,
  `guardianPhone`→`guardian_phone`, `dueDate`→`due_date`, `amountDue`→`amount_due`,
  `amountPaid`→`amount_paid`, `paidOn`→`paid_on`, `startDate`→`start_date`,
  `endDate`→`end_date`, `startTime`→`start_time`, `endTime`→`end_time`,
  `displayOrder`→`display_order`, `errorCount`→`error_count`, `currencyCode`→`currency_code`,
  `invitationCode`→`invitation_code`, `expiresAt`→`expires_at`. No column stays camelCase.

**Backend i18n:** no hardcoded message. Keys live in `messages_fr.properties` /
`messages_en.properties` (+ `messages_ar.properties`, created empty or translated depending on
version). Exceptions resolved via `MessageSource`.

### Frontend web — Next.js

- One `features/<feature>/` folder (logic) + routes under `app/`. See `features/users`,
  `features/role-groups`.
- **No manual HTTP calls**: regenerate the Kubb client (`pnpm --filter @houndjo/apiclient
  generate` or equivalent) after any OpenAPI change, then consume the generated TanStack Query
  hooks.
- Forms: React Hook Form + Zod. Tables: TanStack Table (reuse the existing `DataTable`).
  Lightweight global state: Zustand.
- i18n: `next-intl`, keys in `src/messages/{fr,en}.json`. No hardcoded string.
- **Responsive is mandatory**: Tailwind `sm/md/lg` breakpoints. Tables → stacked cards on
  mobile; actions reachable by thumb.
- **RTL-ready**: logical properties (`ms-*`/`me-*`/`ps-*`/`pe-*`, `text-start`/`text-end`)
  instead of `ml/mr/pl/pr/text-left/right`.

### Frontend mobile — Expo + BNA UI

- **BNA UI** (ui.ahmedbna.com): copy-source lib (like shadcn) — components copied into the
  project, imported via `@/components/ui/*`, `@/components/charts/*`, `@/hooks/*`,
  `@/theme/*`.
- **Pure React Native**: `StyleSheet` + `style` prop, **never `className`**, no Tailwind, no
  DOM.
- Colors via the `useColor` hook — **never hardcoded hex**. Sizes via `HEIGHT`, `FONT_SIZE`,
  `BORDER_RADIUS`, `CORNERS` tokens from `@/theme/globals`.
- Providers: `ThemeProvider` + `ModeProvider` (light/dark), reconciled with the app's existing
  appearance system (see ticket E-M-1).
- Same shared Kubb client as the web app.

### Definition of Done (every ticket)

1. Code follows the conventions above.
2. Tests: backend → Testcontainers integration test covering the nominal path + errors +
   **tenant isolation**; frontend → at minimum the happy path compiles and renders without
   error.
3. i18n: FR keys present (EN/AR depending on version), zero hardcoded string.
4. Liquibase migration applied and reversible; OpenAPI regenerated; Kubb client regenerated.
5. Web responsive verified; mobile rendered on iOS + Android.
6. `@PreAuthorize` in place and the permission seeded in DML.

See also `docs/coding-convention.md` for generic naming/style conventions (file naming, generic
REST/DB/git conventions) that apply project-wide beyond this domain-specific backlog.
