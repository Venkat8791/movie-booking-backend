package com.mxmovies.show.dto.response;


import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ShowsByTheatreResponse {
    private final UUID theatreId;
    private final String theatreName;
    private final String theatreAddress;
    private final List<ScreenShows> screens;

    @Getter
    @Builder
    public static class ScreenShows{
        private final UUID screenId;
        private final String screenName;
        private final String screenType;
        private final List<ShowSummary> shows;
    }

    @Getter
    @Builder
    public static class ShowSummary{
        private final UUID showId;
        private final String showTime;
        private final Double priceMultiplier;
        private final String status;
    }
}
