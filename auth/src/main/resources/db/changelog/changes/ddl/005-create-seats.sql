--liquibase formatted sql
--changeset venkatgadiyakari:05_create_seats

CREATE TABLE seats(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    screen_id UUID NOT NULL,
    row_label VARCHAR(5) NOT NULL,
    column_number INT NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    price_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.0,
    CONSTRAINT fk_screen FOREIGN KEY(screen_id) REFERENCES screens(id) ON DELETE CASCADE,
    CONSTRAINT uq_seat UNIQUE(screen_id,row_label, column_number)
)