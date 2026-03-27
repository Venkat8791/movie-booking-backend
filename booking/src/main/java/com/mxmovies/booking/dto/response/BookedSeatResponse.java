package com.mxmovies.booking.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class BookedSeatResponse {
    private final UUID seatId;
    private final String seatLabel;
    private final String seatType;
    private final BigDecimal price;
}
