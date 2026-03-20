package com.mxmovies.theatre.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ScreenRequest {

    @NotBlank(message = "Screen name is required")
    private String name;

    @NotBlank(message = "Screen type is required")
    private String screenType;

    @NotNull(message = "Total rows is required")
    @Min(value = 1, message = "Total rows must be atleast 1")
    private Integer totalRows;

    @NotNull(message = "Total columns is required")
    @Min(value = 1, message = "Total columns must be at least 1")
    private Integer totalColumns;
}
