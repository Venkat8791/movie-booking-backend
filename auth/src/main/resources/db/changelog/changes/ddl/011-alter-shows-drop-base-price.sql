--liquibase formatted sql
--changeset venkatgadiyakari:011-alter-shows-drop-base-price

ALTER TABLE shows DROP COLUMN base_price;
