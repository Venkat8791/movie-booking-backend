package com.mxmovies.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;
}
