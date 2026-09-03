--liquibase formatted sql
--changeset houndjo:00004-insert-organization-permission

INSERT INTO permission (code, description)
VALUES ('organization:read', 'View any organization details (admin)');

INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code = 'organization:read'
WHERE rg.name IN ('Sysadmin', 'Admin');
