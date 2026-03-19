--liquibase formatted sql
--changeset venkatgadiyakari:06_create_shows

CREATE TABLE shows (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    movie_id   UUID NOT NULL,
    screen_id  UUID NOT NULL,
    show_time  TIMESTAMP NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- 'ACTIVE', 'CANCELLED', 'COMPLETED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movie  FOREIGN KEY (movie_id)  REFERENCES movies(id)  ON DELETE CASCADE,
    CONSTRAINT fk_screen FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE CASCADE
);