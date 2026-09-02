--liquibase formatted sql
--changeset saliou673:00003-role-group-create-table

CREATE TABLE IF NOT EXISTS role_group
(
    id               BIGSERIAL    NOT NULL,
    name             VARCHAR(100) NOT NULL,
    description      TEXT         NOT NULL,
    creation_date    TIMESTAMP    NOT NULL DEFAULT now(),
    last_update_date TIMESTAMP    NOT NULL DEFAULT now(),
    last_updated_by  VARCHAR(255) NOT NULL DEFAULT 'system',
    CONSTRAINT pk_role_group PRIMARY KEY (id),
    CONSTRAINT uq_role_group_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS role_group_permission
(
    role_group_id   BIGINT       NOT NULL REFERENCES role_group (id) ON DELETE CASCADE,
    permission_code VARCHAR(100) NOT NULL REFERENCES permission (code) ON DELETE CASCADE,
    CONSTRAINT pk_role_group_permission PRIMARY KEY (role_group_id, permission_code)
);
