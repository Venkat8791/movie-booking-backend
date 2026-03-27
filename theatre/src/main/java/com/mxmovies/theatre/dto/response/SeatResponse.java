package com.mxmovies.theatre.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class SeatResponse {
    private final UUID id;
    private final UUID screenId;
    private final String seatLabel;
    private final String seatType;
    private final BigDecimal priceMultiplier;
}
