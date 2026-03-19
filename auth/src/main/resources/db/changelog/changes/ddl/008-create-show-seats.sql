--liquibase formatted sql
--changeset venkatgadiyakari:08_create_show_seats

CREATE TABLE show_seats (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    show_id    UUID NOT NULL,
    seat_id    UUID NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING' or 'BOOKED'
    booking_id UUID,
    price      DECIMAL(10,2),
    CONSTRAINT fk_show    FOREIGN KEY (show_id)    REFERENCES shows(id)  ON DELETE CASCADE,
    CONSTRAINT fk_seat    FOREIGN KEY (seat_id)    REFERENCES seats(id)  ON DELETE CASCADE,
    CONSTRAINT fk_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    CONSTRAINT uq_show_seat UNIQUE (show_id, seat_id)
);