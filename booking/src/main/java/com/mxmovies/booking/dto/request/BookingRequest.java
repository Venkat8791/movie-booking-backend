package com.mxmovies.booking.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class BookingRequest {

    @NotNull(message = "Show is required")
    private UUID showId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<UUID> seatIds;
}
