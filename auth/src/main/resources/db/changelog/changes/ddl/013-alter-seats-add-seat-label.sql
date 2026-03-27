--liquibase formatted sql
--changeset venkatgadiyakari:013-alter-seat-add-seat-label

ALTER TABLE seats ADD COLUMN seat_label VARCHAR(4);