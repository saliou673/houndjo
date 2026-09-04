--liquibase formatted sql
--changeset houndjo:00008-insert-class-permissions

INSERT INTO permission (code, description)
VALUES ('class:read', 'View classes (grade/class levels)'),
       ('class:create', 'Create classes'),
       ('class:update', 'Update classes'),
       ('class:delete', 'Delete classes');

INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code IN ('class:read', 'class:create', 'class:update', 'class:delete')
WHERE rg.name IN ('Sysadmin', 'Admin', 'User');
