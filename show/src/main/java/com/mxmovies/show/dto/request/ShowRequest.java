package com.mxmovies.show.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ShowRequest {

    @NotNull(message = "Movie is required")
    private UUID movieId;

    @NotNull(message = "Screen is required")
    private UUID screenId;

    @NotNull(message = "Show time is required")
    @Future(message = "Show time must be in the future")
    private LocalDateTime showTime;

    @NotNull(message = "Price multiplier is required")
    @DecimalMin(value = "1.0", message = "Price multiplier must be at least 1.0")
    private BigDecimal priceMultiplier;
}
