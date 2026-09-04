--liquibase formatted sql
--changeset houndjo:00010-insert-student-permissions

INSERT INTO permission (code, description)
VALUES ('student:read', 'View students'),
       ('student:create', 'Create students'),
       ('student:update', 'Update students'),
       ('student:delete', 'Delete students');

INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code IN ('student:read', 'student:create', 'student:update', 'student:delete')
WHERE rg.name IN ('Sysadmin', 'Admin', 'User');
