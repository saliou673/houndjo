--liquibase formatted sql
--changeset houndjo:00005-insert-membership-permissions

INSERT INTO permission (code, description)
VALUES ('membership:read', 'View organization memberships (admin)'),
       ('membership:create', 'Create organization memberships (admin)'),
       ('membership:update', 'Change an organization membership''s role (admin)'),
       ('membership:delete', 'Revoke organization memberships (admin)');

INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code IN ('membership:read', 'membership:create', 'membership:update', 'membership:delete')
WHERE rg.name IN ('Sysadmin', 'Admin');
