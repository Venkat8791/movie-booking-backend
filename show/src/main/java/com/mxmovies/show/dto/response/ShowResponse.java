package com.mxmovies.show.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ShowResponse {
    private final UUID id;
    private final UUID movieId;
    private final UUID screenId;
    private final LocalDateTime showTime;
    private final BigDecimal priceMultiplier;
    private final String status;
    private final LocalDateTime createdAt;
}
