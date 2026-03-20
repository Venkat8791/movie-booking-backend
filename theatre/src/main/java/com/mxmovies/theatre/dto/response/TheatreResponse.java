package com.mxmovies.theatre.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TheatreResponse {
    private final UUID id;
    private final String name;
    private final String city;
    private final String address;
    private final String pincode;
}
