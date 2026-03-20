--liquibase formatted sql
--changeset venkatgadiyakari:012-alter-bookings-add-expires-at

ALTER TABLE bookings ADD COLUMN expires_at TIMESTAMP;