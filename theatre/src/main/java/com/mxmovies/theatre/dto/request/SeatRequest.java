package com.mxmovies.theatre.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SeatRequest {

    @NotBlank(message = "Row label is required")
    private String rowLabel;

    @NotNull(message = "Column number is required")
    private Integer columnNumber;

    @NotBlank(message = "Seat type is required")
    private String seatType;  // REGULAR, PREMIUM, RECLINER

    @NotNull(message = "Price multiplier is required")
    @DecimalMin(value = "1.0", message = "Price multiplier must be at least 1.0")
    private BigDecimal priceMultiplier;
}
