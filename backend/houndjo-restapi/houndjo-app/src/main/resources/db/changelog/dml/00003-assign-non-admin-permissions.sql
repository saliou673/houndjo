--liquibase formatted sql
--changeset saliou673:00006-assign-non-admin-permissions

-- Backfill the missing read permission used by non-admin configuration access.
INSERT INTO permission (code, description)
SELECT 'config:read', 'View configuration values'
WHERE NOT EXISTS (
    SELECT 1
    FROM permission
    WHERE code = 'config:read'
);

-- Grant non-admin account/config permissions to the User and Admin role groups.
INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code IN ('user:read:own', 'user:update:own', 'config:read')
WHERE rg.name IN ('User', 'Admin')
  AND NOT EXISTS (
    SELECT 1
    FROM role_group_permission rgp
    WHERE rgp.role_group_id = rg.id
      AND rgp.permission_code = p.code
);
