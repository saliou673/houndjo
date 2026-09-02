---
name: "implement-epic"
description: "Recursively implement every open sub-issue under a houndjo GitHub epic issue: picks the next actionable sub-issue in order, creates a stacked branch, implements it as a senior Java/Spring (hexagonal) + Next.js/Expo developer, commits, opens a PR against the previous branch in the stack, then repeats for the next issue."
argument-hint: "<epic-issue-number-or-url>"
metadata:
  author: "houndjo"
user-invocable: true
disable-model-invocation: true
---

## User input

```text
$ARGUMENTS
```

This is the parent **epic** issue — a number (`62`), `#62`, or a full GitHub URL. If empty, ask the user for it before doing anything else.

Repo: `saliou673/spring-next-saas-starter` (use `gh` with `--repo saliou673/spring-next-saas-starter`, or omit `--repo` when running from this repo root, which already points at that remote).

This skill is a loop: resolve the epic → pick the next actionable sub-issue → implement it end-to-end → commit → PR → repeat, until nothing actionable remains. Invoking this skill is the user's standing authorization to branch, commit, push, and open PRs for each issue it handles — don't re-confirm those mechanical steps per issue. Do stop and ask the user (plain question, or `AskUserQuestion` for multiple-choice) whenever an issue's requirements are genuinely ambiguous, underspecified, or admit more than one reasonable implementation — never guess on business rules.

Never kill or restart the user's running dev servers (backend `mvn spring-boot:run`, frontend `pnpm dev`) while verifying a change — check if one is already up before starting your own, and leave it running afterward. Never add a `Co-Authored-By` (or any assistant co-author) trailer to commits in this repo.

## 0. Read the conventions once, up front

Before touching any code, read:
- `docs/coding-convention.md` — naming (Controller/Service/Repository/DTO suffixes, kebab-case URLs), commit message format (`<type>(<scope>): <subject>`), branch/PR best practices.
- `docs/permissions.md` — the RBAC model: role groups, permission codes (`resource:action`, `resource:action:own`), and how `@PreAuthorize("hasAuthority('permission:code')")` gates endpoints. Any new resource needs its permission rows added here (conceptually) and seeded in a migration.
- `README.md` §"Project structure" and §"Testing" — module layout and the exact build/test/lint commands (reproduced in step 6 below).
- The **RoleGroup** backend slice and **role-groups** frontend feature are the canonical worked examples for a full CRUD resource — read the actual files, not just the docs, when a ticket's shape is unclear:
  - Backend: `backend/houndjo-restapi/houndjo-core/src/main/java/com/houndjo/domain/models/rbac/RoleGroup.java`, `domain/ports/in/RoleGroupUseCase.java`, `domain/ports/out/persistenceport/RoleGroupPersistencePort.java`, `domain/exceptions/RoleGroupNotFoundException.java` (+`RoleGroupNameAlreadyExistsException.java`); `backend/houndjo-restapi/houndjo-app/src/main/java/com/houndjo/application/RoleGroupService.java`; `infrastructure/adapter/out/persistence/{entity/RoleGroupEntity.java, repository/RoleGroupRepository.java, mapper/RoleGroupMapper.java, RoleGroupPersistenceAdapter.java}`; `infrastructure/adapter/in/rest/controller/{AdminRoleGroupController.java, dto/RoleGroupDTO.java, mapper/RoleGroupDtoMapper.java, requests/{Create,Update,Assign}RoleGroupRequest.java}`; migrations `houndjo-app/src/main/resources/db/changelog/ddl/00003-role-group-create-table.sql` and `ddl/00004-app-user-role-group-create-table.sql` plus the seed DML in `dml/00002-insert-default-role-groups.sql`; test `houndjo-tests/src/test/java/com/houndjo/integration/controller/AdminRoleGroupControllerTest.java`.
  - Frontend (web): `frontend/apps/houndjo-web/src/features/role-groups/` (`index.tsx`, `components/role-groups-{table,columns,action-dialog,delete-dialog,dialogs,primary-buttons,provider}.tsx`, `data/schema.ts`), wired under `frontend/apps/houndjo-web/app/role-groups/`.
  - Frontend (mobile, when a ticket targets `houndjo-mobile`): the existing `(app)/settings/*` screens under `frontend/apps/houndjo-mobile/src/app/` are the closest template for a feature screen wired to the shared API client and i18next.

Remembered project preference: this app is bilingual (English/French) via `next-intl` on web (`frontend/apps/houndjo-web/src/messages/{en,fr}.json`) and `react-i18next` on mobile (`frontend/apps/houndjo-mobile/src/i18n/locales/{en,fr}.json`). Every user-facing string added or changed must get a key in **both** locale files on the platform(s) touched — never hardcode English (or French) text in a component. Routes, identifiers, and code stay in English.

## 1. Resolve the epic and build the ordered sub-issue list

```
gh issue view <epic-number> --repo saliou673/spring-next-saas-starter --json number,title,body,labels,state
gh api graphql -f query='
query { repository(owner:"saliou673", name:"spring-next-saas-starter") {
  issue(number: <epic-number>) { title body subIssues(first:50) { nodes { number title state } } }
}}'
```

- Epics in this repo carry the `epic` label plus a scoped label like `epic:account-settings` or `epic:error-states`; the epic body is usually a one- or two-line description, not a checklist — GitHub's native **sub-issues** (`subIssues` in the query above) are the authoritative ordered list of implementable issues, not a body checklist.
- Extract, per sub-issue, in order: issue number, title (titles are prefixed with scope tags like `[Web]`, `[Mobile]`, `[Backend]`, or a combination like `[Backend][Web][Mobile]` — this is the ticket's `Type`), and open/closed state.
- If a sub-issue's own labels show it belongs to a *different* `epic:*` label than the parent (a cross-epic tracking link), note it but don't implement it here — treat it like any other epic's issue and skip it in this run.

## 2. Pick the next actionable sub-issue

Walk the ordered list top to bottom. For each issue:

1. **Closed issue → skip.** Already done.
2. **Open issue, but already has work in flight → skip.** Check both:
   ```
   git branch -a --list "<issue-number>-*"
   gh pr list --repo saliou673/spring-next-saas-starter --search "<issue-number> in:body" --state all --json number,title,headRefName,baseRefName,state
   ```
   If a branch and/or PR already exists for this issue number, treat it as handled — move to the next issue. Do not reimplement or touch it.
3. **Open issue, no existing branch/PR → this is the target issue.** Stop walking, proceed to step 3.

If you reach the end of the list with no target issue, **stop the recursion**: report to the user which issues were skipped (closed / already in flight) and confirm the epic has no remaining actionable work.

## 3. Read the issue in full

```
gh issue view <issue-number> --repo saliou673/spring-next-saas-starter --json title,body,labels
```

Issue bodies in this repo are short: a description paragraph (often naming the specific existing file it should mirror or extend, e.g. "matching `frontend/apps/houndjo-web/app/errors/forbidden`") and a trailing `Part of #<epic>` line — there's no separate `Acceptance criteria` section, so the description *is* the spec. When it references a sibling web/mobile screen or an existing backend endpoint as the thing to mirror, read that referenced file before writing any code — it's the real acceptance criteria.

If, after reading the issue and the file(s) it points at, there's a genuine open question (ambiguous field type, unclear relationship, multiple valid UX choices, a missing dependency that isn't actually ready) — **ask the user now**, before writing any code. Otherwise proceed.

## 4. Create the branch (stacked)

- Determine the **base branch**: on the very first issue handled in this run, base = the current branch (`git branch --show-current`) — i.e. the current stack tip. On every subsequent issue in this same run, base = the branch you just opened a PR for in the previous iteration. This mirrors the repo's existing stacked-PR convention (each issue's PR bases on the previous issue's branch — there is no bot that re-parents PRs automatically here, so this chaining is done by hand, by you, each time).
- Before branching, `git status` — if the working tree is dirty with unrelated changes, stop and ask the user how to proceed rather than branching over it.
- Branch name: `<issue-number>-<kebab-case-slug-of-title>`, dropping the `[Web]`/`[Mobile]`/`[Backend]` bracket tags from the slug. Match existing history, e.g. `108-mobile-totp-qr`, `33-language-switcher`, `107-display-preferences`.
  ```
  git checkout -b <issue-number>-<slug> <base-branch>
  ```

## 5. Implement

Determine backend/web/mobile scope from the issue title's bracket tags (`[Backend]`, `[Web]`, `[Mobile]`, or a combination).

**Backend (Java / Spring Boot, hexagonal — `backend/houndjo-restapi`)**
Follow the RoleGroup slice exactly, layer by layer:
`houndjo-core/.../domain/models/<area>/<Entity>.java` (plain domain object, private ctor + `create`/`rehydrate` factories, `update(...)`) → `domain/ports/in/<Entity>UseCase.java` → `domain/ports/out/persistenceport/<Entity>PersistencePort.java` → domain exceptions in `domain/exceptions/` → `houndjo-app/.../application/<Entity>Service.java` (implements the use case, calls the port, enforces invariants) → `infrastructure/adapter/out/persistence/entity/<Entity>Entity.java` + `repository/<Entity>Repository.java` + `mapper/<Entity>Mapper.java` (MapStruct) + `<Entity>PersistenceAdapter.java` → `infrastructure/adapter/in/rest/controller/requests/Create<Entity>Request.java`/`Update<Entity>Request.java` (records, `jakarta.validation`) + `dto/<Entity>DTO.java` + `mapper/<Entity>DtoMapper.java` + `controller/<Entity>Controller.java` (`Admin<Entity>Controller.java` if it's an admin-scoped resource, matching `AdminRoleGroupController`/`AdminUserController`; `@PreAuthorize("hasAuthority('<resource>:<action>')")` per docs/permissions.md, `Pageable`/paginated result for list endpoints).
- Migration: Liquibase changeset under `houndjo-app/src/main/resources/db/changelog/ddl/`, standard audit columns, registered in the changelog master file — one migration file per table. New permission codes seeded via a DML changeset under `db/changelog/dml/`, following `docs/permissions.md`'s naming (`resource:action`, `resource:action:own`) and wired into the right role groups.
- Keep `houndjo-core` framework-agnostic — no JPA/Spring annotations there.
- Add/extend integration tests in `houndjo-tests` (`<Entity>ControllerTest` — mirror the depth of `AdminRoleGroupControllerTest`).
- Backend error messages go through `MessageSource` (English/French) — see the domain exception localization pattern already in place — not hardcoded strings.

**Frontend web (Next.js — `frontend/apps/houndjo-web`)**
Follow the `role-groups` feature template: `src/features/<module>/index.tsx`, `components/<module>-table.tsx` + `-columns.tsx`, `-action-dialog.tsx`, `-delete-dialog.tsx`, `-dialogs.tsx`, `-primary-buttons.tsx`, `-provider.tsx`, `data/schema.ts` (zod). Wire the page under `app/<module>/`.
- Data access **only** through generated hooks in `frontend/packages/houndjo-apiclient/src/gen/react-query` — never hand-write fetch calls. If the backend contract changed or is new, regenerate the client (`pnpm generate:api`, backend must be running) once the backend endpoints exist/compile.
- All visible UI text via `useTranslations("<Namespace>")` from `next-intl`, with matching keys added to both `src/messages/en.json` and `src/messages/fr.json`.

**Frontend mobile (Expo / React Native — `frontend/apps/houndjo-mobile`)**
Follow the existing `(app)/settings/*` screens as the template for a feature screen wired to `expo-router` and the shared `houndjo-apiclient` hooks.
- All visible UI text via `react-i18next`'s `useTranslation`, with matching keys added to both `src/i18n/locales/en.json` and `src/i18n/locales/fr.json`.

**Multi-scope tickets** (title has more than one bracket tag, e.g. `[Backend][Web][Mobile]`): do the backend slice first (so the API exists to generate the client against), then web, then mobile, as separate commits (see below) — this matches this repo's history for cross-cutting tickets (see PR #116, #117).

## 6. Verify before committing

- Backend: `mvn test` from `backend/houndjo-restapi` (or `mvn -pl houndjo-app -am test` / `-pl houndjo-tests -am test` if the change is isolated to one module) — must be green.
- Frontend: from `frontend/`, `pnpm lint && pnpm format:check && pnpm build` (or narrower: `pnpm --filter houndjo-webapp build`, `tsc --noEmit` for a quick web check; `tsc --noEmit` in `apps/houndjo-mobile` for mobile) — zero new errors/warnings.
- Where practical, run the app locally and exercise the golden path for the feature before opening the PR. Reuse an already-running dev server if one exists (`curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/` / `:8080`) — never kill or restart the user's own server.

## 7. Commit by concern

Split into focused commits matching this repo's actual granularity: not a rigid one-commit-per-hexagonal-layer breakdown, but one commit per real concern — typically one commit for the backend slice (domain through controller, plus its migration and tests), one for `chore(apiclient): regenerate client for <Entity> endpoints` when the frontend needs a new/changed contract, and one per frontend platform touched. Follow up with small `fix(...)` commits if verification turns up something broken, rather than folding fixes silently into the original commit. Typical shape for a multi-scope issue:

```
feat(backend): add <Entity> domain model, persistence, and API (<short-desc>)
chore(apiclient): regenerate client for <Entity> endpoints
feat(web): add <feature> to <page> (#<issue-number>)
feat(mobile): add <feature> screen (#<issue-number>)
```

Use `<type>(<scope>): <subject>` from `docs/coding-convention.md`, scopes like `backend`, `web`, `mobile`, or comma-joined (`backend,web,mobile`) when one commit genuinely spans platforms — matches existing history (e.g. `feat(backend,web,mobile): persist real notification preferences`). Wrap the commit body with the *why*, not a restatement of the diff — see `git log` on `houndjo-app`/`houndjo-web` for the tone to match. **Do not add a `Co-Authored-By` trailer.**

## 8. Push and open the PR

```
git push -u origin <issue-number>-<slug>
gh pr create --repo saliou673/spring-next-saas-starter \
  --base <base-branch> --head <issue-number>-<slug> \
  --title "<issue title, unchanged>" \
  --label enhancement \
  --body "$(cat <<'EOF'
## Summary
- <what changed, bullet per concern>

## Verification
- [x] <backend test command/result>
- [x] <frontend build/lint result>
- [x] <manual end-to-end verification, if performed>

Closes #<issue-number>
EOF
)"
```

Mirror the real style used in this repo's PRs (see PR #113, #116 for reference): concrete `## Summary` bullets, an optional `## Design notes` / `## Why ...` / `## Backward compatibility` section when there's a non-obvious tradeoff worth explaining, a `## Verification` checklist with actual results (not placeholders), and a trailing plain `Closes #N` line. Carry over any scope-specific label already used on the parent epic's other sub-issues (e.g. `mobile`) in addition to `enhancement` if applicable — check `gh label list` / sibling issues' labels rather than assuming.

## 9. Recurse

The branch just opened becomes the new stack tip. Go back to **step 2** and pick the next actionable issue in the epic, using this branch as the base for the next one. Continue until step 2 finds nothing actionable, then stop and give the user a short summary: issues implemented (with PR links), issues skipped as already in flight, issues skipped as closed, and anything you had to ask about along the way.
