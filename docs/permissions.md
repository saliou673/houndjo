# Permissions & Role Groups

Houndjo uses a fine-grained Role-Based Access Control (RBAC) system. Users are assigned one or more **role groups
**,
and each role group holds a set of **permissions**. Spring Security enforces permissions at the method level via
`@PreAuthorize("hasAuthority('permission:code')")`.

---

## How It Works

```
User ──▶ RoleGroup(s) ──▶ Permission(s) ──▶ JWT claims ──▶ Spring Security
```

1. A user is assigned one or more role groups.
2. `User.resolvePermissions()` flattens all permissions from all assigned role groups into a single set.
3. Permission codes are embedded in the JWT token as claims.
4. Spring Security checks each endpoint against the token's permission codes.

---

## Role Groups

Six role groups are seeded by default. Each group targets a specific user profile.

| Group         | Description                                                       |
|---------------|-------------------------------------------------------------------|
| **Sysadmin**  | Full system access. Intended for platform administrators.         |
| **Admin**     | Site management access. Can manage users, read configurations,... |
| **User**      | Standard authenticated customer. Can purchase, learning session   |
| **Anonymous** | Guest user .                                                      |

---

## Permission Matrix

Permissions follow a naming convention: plain codes (e.g. `user:read`) grant admin-scoped access across all
resources; codes with the `:own` suffix (e.g. `user:read:own`) grant access only to the caller's own resources.

### User management

| Permission        | Description                         | Sysadmin | Admin | User | Anonymous |
|-------------------|-------------------------------------|:--------:|:-----:|:----:|:---------:|
| `user:read`       | View any user details (admin)       |    ✅     |   ✅   |  ❌   |     ❌     |
| `user:create`     | Create new users                    |    ✅     |   ✅   |  ❌   |     ❌     |
| `user:update`     | Update any user information (admin) |    ✅     |   ✅   |  ❌   |     ❌     |
| `user:deactivate` | Deactivate user accounts            |    ✅     |   ✅   |  ❌   |     ❌     |
| `user:invite`     | Invite users by email               |    ✅     |   ✅   |  ❌   |     ❌     |
| `user:read:own`   | View own account details            |    ✅     |   ✅   |  ✅   |     ❌     |
| `user:update:own` | Update own account and password     |    ✅     |   ✅   |  ✅   |     ❌     |

### Configuration

| Permission      | Description                                          | Sysadmin | Admin | User | Anonymous |
|-----------------|------------------------------------------------------|:--------:|:-----:|:----:|:---------:|
| `config:read`   | View application configurations                      |    ✅     |   ✅   |  ❌   |     ❌     |
| `config:manage` | Create, update and delete application configurations |    ✅     |   ❌   |  ❌   |     ❌     |

### Subscription plans

| Permission    | Description                    | Sysadmin | Admin | User | Anonymous |
|---------------|--------------------------------|:--------:|:-----:|:----:|:---------:|
| `plan:read`   | View subscription plan details |    ✅     |   ✅   |  ❌   |     ❌     |
| `plan:create` | Create subscription plans      |    ✅     |   ❌   |  ❌   |     ❌     |
| `plan:update` | Update subscription plans      |    ✅     |   ❌   |  ❌   |     ❌     |
| `plan:delete` | Delete subscription plans      |    ✅     |   ❌   |  ❌   |     ❌     |

### Discount codes

| Permission             | Description                | Sysadmin | Admin | User | Anonymous |
|------------------------|----------------------------|:--------:|:-----:|:----:|:---------:|
| `discount-code:read`   | View discount code details |    ✅     |   ❌   |  ❌   |     ❌     |
| `discount-code:create` | Create discount codes      |    ✅     |   ❌   |  ❌   |     ❌     |
| `discount-code:update` | Update discount codes      |    ✅     |   ❌   |  ❌   |     ❌     |
| `discount-code:delete` | Delete discount codes      |    ✅     |   ❌   |  ❌   |     ❌     |
