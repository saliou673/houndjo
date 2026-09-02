--liquibase formatted sql
--changeset saliou673:00002-insert-default-role-groups

-- Insert role groups mirroring existing authorities
INSERT INTO role_group (name, description, last_updated_by)
VALUES ('Sysadmin', 'System administrator — full access', 'system'),
       ('Admin', 'Site administrator', 'system'),
       ('User', 'Public customer — that learn on the platform', 'system'),
       ('Anonymous', 'Anonymous user — guest user', 'system');

-- Sysadmin → all permissions
INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg,
     permission p
WHERE rg.name = 'Sysadmin';

-- Admin → user management, config read, tax stamp type read, stamp admin operations
INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code IN (
                                         'user:read', 'user:create', 'user:update', 'user:deactivate', 'user:invite',
                                         'config:read',
                                         'role-group:read'
    )
WHERE rg.name = 'Admin';
