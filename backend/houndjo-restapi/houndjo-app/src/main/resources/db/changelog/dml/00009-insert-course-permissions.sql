--liquibase formatted sql
--changeset houndjo:00009-insert-course-permissions

INSERT INTO permission (code, description)
VALUES ('course:read', 'View courses'),
       ('course:create', 'Create courses'),
       ('course:update', 'Update courses'),
       ('course:delete', 'Delete courses');

INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code IN ('course:read', 'course:create', 'course:update', 'course:delete')
WHERE rg.name IN ('Sysadmin', 'Admin', 'User');
