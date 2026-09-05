--liquibase formatted sql
--changeset houndjo:00016-insert-billing-permissions

INSERT INTO permission (code, description)
VALUES ('billing:manage', 'Manage fee schedules, dues and payments');

INSERT INTO role_group_permission (role_group_id, permission_code)
SELECT rg.id, p.code
FROM role_group rg
         JOIN permission p ON p.code = 'billing:manage'
WHERE rg.name IN ('Sysadmin', 'Admin', 'User');
