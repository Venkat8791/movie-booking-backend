--liquibase formatted sql
--changeset venkatgadiyakari:04_create_movies

CREATE TABLE movies(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    language  VARCHAR(50) NOT NULL,
    genre VARCHAR(100),
    duration_minutes INT NOT NULL,
    poster_url TEXT,
    rating VARCHAR(10),
    release_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)