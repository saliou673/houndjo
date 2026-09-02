--liquibase formatted sql
--changeset saliou673:00007-two-factor-challenge-create-table

CREATE TABLE IF NOT EXISTS two_factor_challenge
(
    id            VARCHAR(36)              NOT NULL,
    user_id       BIGINT                   NOT NULL,
    code          TEXT                     NOT NULL,
    type          VARCHAR(50)              NOT NULL,
    purpose       VARCHAR(20)              NOT NULL,
    remember_me   BOOLEAN                  NOT NULL DEFAULT FALSE,
    expiry_date   TIMESTAMP WITH TIME ZONE NOT NULL,
    creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_two_factor_challenge PRIMARY KEY (id),
    CONSTRAINT fk_two_factor_challenge_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_2fa_challenge_user_id ON two_factor_challenge (user_id);
