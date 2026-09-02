--liquibase formatted sql
--changeset saliou673:00012-user-preference-create-table

CREATE TABLE IF NOT EXISTS user_preference
(
    user_id           BIGINT    NOT NULL,
    preferences       JSONB     NOT NULL,
    creation_date     TIMESTAMP NOT NULL,
    last_update_date  TIMESTAMP NOT NULL,
    last_updated_by   TEXT      NOT NULL,
    CONSTRAINT pk_user_preference PRIMARY KEY (user_id),
    CONSTRAINT fk_user_preference_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);
