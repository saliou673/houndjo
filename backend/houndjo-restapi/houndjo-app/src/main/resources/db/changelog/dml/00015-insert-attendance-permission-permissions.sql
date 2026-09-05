--liquibase formatted sql
--changeset houndjo:00015-insert-attendance-permission-permissions

INSERT INTO permission (code, description)
VALUES ('attendance-permission:read', 'View student leave/absence authorizations'),
       ('attendance-permission:create', 'Request a student leave/absence authorization'),
       ('attendance-permission:update', 'Approve or reject a student leave/absence authorization');

INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code IN
             ('attendance-permission:read', 'attendance-permission:create', 'attendance-permission:update')
WHERE rg.name IN ('Sysadmin', 'Admin', 'User');
