--liquibase formatted sql
--changeset houndjo:00012-insert-session-permissions

INSERT INTO permission (code, description)
VALUES ('session:read', 'View sessions'),
       ('session:create', 'Create sessions'),
       ('session:update', 'Update sessions'),
       ('session:delete', 'Delete sessions');

INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code IN
             ('session:read', 'session:create', 'session:update', 'session:delete')
WHERE rg.name IN ('Sysadmin', 'Admin', 'User');
