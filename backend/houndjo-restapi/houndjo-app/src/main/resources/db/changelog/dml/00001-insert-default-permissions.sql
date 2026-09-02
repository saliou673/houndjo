--liquibase formatted sql
--changeset saliou673:00001-insert-default-permissions

INSERT INTO permission (code, description)
VALUES
    -- User management (admin scope)
    ('user:read', 'View any user details (admin)'),
    ('user:create', 'Create new users'),
    ('user:update', 'Update any user information (admin)'),
    ('user:deactivate', 'Deactivate user accounts'),
    ('user:invite', 'Invite users by email'),

    -- User management (own scope)
    ('user:read:own', 'View own account details'),
    ('user:update:own', 'Update own account information and password'),


    -- Role group management
    ('role-group:read', 'View role groups and their permissions'),
    ('role-group:create', 'Create role groups'),
    ('role-group:update', 'Update role groups'),
    ('role-group:delete', 'Delete role groups'),

    -- Configuration
    ('config:read', 'View configuration values'),
    ('config:create', 'Create configuration entries'),
    ('config:update', 'Update configuration entries'),
    ('config:delete', 'Delete configuration entries');
