package com.mxmovies.theatre.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ScreenResponse {
    private final UUID id;
    private final UUID theatreId;
    private final String name;
    private final String screenType;
    private final Integer totalRows;
    private final Integer totalColumns;
}
