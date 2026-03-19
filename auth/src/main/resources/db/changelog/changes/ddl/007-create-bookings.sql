--liquibase formatted sql
--changeset venkatgadiyakari:07_create_bookings

CREATE TABLE bookings (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id        UUID NOT NULL,
    show_id        UUID NOT NULL,
    total_amount   DECIMAL(10,2) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',   -- 'PENDING', 'CONFIRMED', 'CANCELLED'
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',    -- 'UNPAID', 'PAID', 'REFUNDED'
    booked_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_show FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE
);