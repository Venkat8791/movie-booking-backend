--liquibase formatted sql
--changeset venkatgadiyakari:010-alter-seats-shows-multiplier

ALTER TABLE seats DROP COLUMN price_multiplier;

ALTER TABLE shows ADD COLUMN price_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.0;