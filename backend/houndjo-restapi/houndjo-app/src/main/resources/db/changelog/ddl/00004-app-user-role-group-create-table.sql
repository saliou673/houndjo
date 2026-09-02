--liquibase formatted sql
--changeset saliou673:00004-app-user-role-group-create-table

CREATE TABLE IF NOT EXISTS app_user_role_group
(
    user_id       BIGINT NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    role_group_id BIGINT NOT NULL REFERENCES role_group (id) ON DELETE CASCADE,
    CONSTRAINT pk_app_user_role_group PRIMARY KEY (user_id, role_group_id)
);
