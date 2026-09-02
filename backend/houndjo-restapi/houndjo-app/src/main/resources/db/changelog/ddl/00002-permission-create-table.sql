--liquibase formatted sql
--changeset saliou673:00002-permission-create-table

CREATE TABLE IF NOT EXISTS permission
(
    code        VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    CONSTRAINT pk_permission PRIMARY KEY (code)
);
