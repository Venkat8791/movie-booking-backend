--liquibase formatted sql
--changeset venkatgadiyakari:009-alter-users-add-role

ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER'