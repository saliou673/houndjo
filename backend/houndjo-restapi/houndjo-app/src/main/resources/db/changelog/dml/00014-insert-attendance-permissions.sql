--liquibase formatted sql
--changeset houndjo:00014-insert-attendance-permissions

INSERT INTO permission (code, description)
VALUES ('attendance:read', 'View attendance records'),
       ('attendance:create', 'Record attendance'),
       ('attendance:update', 'Correct attendance records'),
       ('attendance:delete', 'Delete attendance records');

INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code IN
             ('attendance:read', 'attendance:create', 'attendance:update', 'attendance:delete')
WHERE rg.name IN ('Sysadmin', 'Admin', 'User');
