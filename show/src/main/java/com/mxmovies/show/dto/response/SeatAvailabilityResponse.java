package com.mxmovies.show.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SeatAvailabilityResponse {
    private final UUID showId;
    private final List<SectionAvailability> sections;

    @Getter
    @Builder
    public static class SectionAvailability{
        private final String sectionName;
        private final String seatType;
        private final BigDecimal basePrice;
        private final BigDecimal finalPrice;
        private final List<RowAvailability> rows;
    }


    @Getter
    @Builder
    public static class RowAvailability {
        private final String label;
        private final List<SeatAvailability> seats;
    }

    @Getter
    @Builder
    public static class SeatAvailability {
        private final UUID seatId;      // null if gap
        private final Integer columnNumber;
        private final boolean isGap;
        private final boolean isBlocked;
        private final String status;               // AVAILABLE, PENDING, BOOKED
    }

}
