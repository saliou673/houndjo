--liquibase formatted sql
--changeset houndjo:00011-insert-enrollment-permissions

INSERT INTO permission (code, description)
VALUES ('enrollment:read', 'View enrollments'),
       ('enrollment:create', 'Create enrollments'),
       ('enrollment:update', 'Update enrollments'),
       ('enrollment:delete', 'Delete enrollments');

INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code IN
             ('enrollment:read', 'enrollment:create', 'enrollment:update', 'enrollment:delete')
WHERE rg.name IN ('Sysadmin', 'Admin', 'User');
