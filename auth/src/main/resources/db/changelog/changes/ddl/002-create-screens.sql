--liquibase formatted sql
--changeset venkatgadiyakari:02_create_screens

CREATE TABLE screens(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    theatre_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    screen_type VARCHAR(20) NOT NULL DEFAULT '2D',
    total_rows INT NOT NULL,
    total_columns INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_theatre FOREIGN KEY (theatre_id) REFERENCES theatres(id) ON DELETE CASCADE
)