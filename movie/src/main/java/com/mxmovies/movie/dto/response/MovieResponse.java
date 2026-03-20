package com.mxmovies.movie.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class MovieResponse {
    private final UUID id;
    private final String title;
    private final String description;
    private final String language;
    private final String genre;
    private final Integer durationMinutes;
    private final String posterUrl;
    private final String rating;
    private final LocalDate releaseDate;
    private final LocalDateTime createdAt;
}
