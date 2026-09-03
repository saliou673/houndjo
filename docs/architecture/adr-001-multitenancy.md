# ADR-001: Multi-tenant isolation baseline

## Status

Accepted

## Context

Houndjo is a multi-tenant SaaS: every school (organization) using the platform must only ever
see its own data. Every business aggregate introduced from E1 onward (organizations, members,
classes, students, progress, attendance, billing, ...) needs a tenant discriminant and a
consistent way to resolve and enforce "the current organization" for a request.

Two standard approaches exist for enforcing this at the persistence layer:

1. **Hibernate `@FilterDef`/`@Filter`**: a session-level filter transparently appended to every
   query for annotated entities, enabled per-request from an interceptor.
2. **Explicit `organization_id` column discriminant**, filtered by every query at the
   persistence-port level, with an application-level guard resolving "the current organization"
   and every use case explicitly passing/checking it.

## Decision

We use option 2: an explicit **`organization_id` column discriminant** on every business table,
enforced by an **explicit application-level guard**, not a Hibernate filter.

- Every business table carries a non-null `organization_id` column, indexed.
- `TenantContext` (`com.houndjo.application.tenant`, `houndjo-app`) exposes
  `Optional<Long> currentOrganizationId()` and `Long requireCurrentOrganizationId()`. It resolves
  the active organization from the current Spring Security context: the `orgId` claim on the
  authenticated `Jwt` principal. There is no implicit fallback — an authenticated request with no
  `orgId` claim resolves to "no active organization".
  - At this stage (E0-1), nothing mints an `orgId` claim yet — `TenantContext` only defines the
    resolution contract. Populating the claim from the user's active `Membership` (once that
    aggregate exists) is E0-2's responsibility; a later ticket may add an explicit
    organization-switch flow if a user belongs to more than one organization.
- `requireCurrentOrganizationId()` throws `MissingTenantException` (a `FunctionalException`, code
  `error.tenant.missing`) when no organization is resolved. `GlobalExceptionHandler` already maps
  every `FunctionalException` to HTTP 400, so no extra handler wiring is needed.
- Every business use case explicitly calls `TenantContext` and passes the resolved
  `organizationId` down; every list `PersistencePort` method takes `organizationId` as an
  explicit parameter; every `Repository` filters on it. There is no cross-cutting interceptor
  that appends the filter automatically.

## Why not a Hibernate filter

- **Simpler to test**: an explicit parameter is visible in every method signature and easy to
  assert on in integration tests (two orgs, two records, assert the list size and IDs). A
  Hibernate filter's effect is implicit and depends on session-level state being enabled at the
  right time, which is easy to forget in a new code path and hard to catch in review.
- **No magic**: filters silently change query results for entities they're enabled on, which is
  surprising for anyone reading a repository method in isolation. An explicit `organizationId`
  parameter documents the constraint at the call site.
- **Fails safe**: forgetting to pass `organizationId` to a persistence-port method is a compile-time
  signature mismatch; forgetting to enable a Hibernate filter is a silent full-tenant data leak
  that only surfaces at runtime.

## Consequences

- Every new aggregate's `PersistencePort` and `Repository` methods must take `organizationId`
  explicitly — there is no way to "opt out" accidentally get tenant filtering for free.
- Every new business use case must call `TenantContext` and enforce the check itself; this is a
  per-ticket code review item until a shared base class/annotation pattern proves itself needed.
- Every business route relies on `MissingTenantException` → HTTP 400 (`error.tenant.missing`)
  rather than a dedicated middleware response for "no active organization".
