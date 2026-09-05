--liquibase formatted sql
--changeset houndjo:00013-insert-progress-permissions

INSERT INTO permission (code, description)
VALUES ('progress:read', 'View progress records'),
       ('progress:create', 'Record progress'),
       ('progress:update', 'Correct progress records'),
       ('progress:delete', 'Delete progress records');

INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code IN
             ('progress:read', 'progress:create', 'progress:update', 'progress:delete')
WHERE rg.name IN ('Sysadmin', 'Admin', 'User');
