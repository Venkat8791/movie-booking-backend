package com.mxmovies.booking.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class BookingResponse {
    private final UUID id;
    private final UUID userId;
    private final UUID showId;
    private final List<BookedSeatResponse> seats;
    private final BigDecimal totalAmount;
    private final String status;
    private final String paymentStatus;
    private final LocalDateTime bookedAt;
    private final LocalDateTime expiresAt;  //bookedAt + 10 mins
}
