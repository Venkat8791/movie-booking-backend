package com.mxmovies.theatre.model.mongo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "seat_layouts")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeatLayoutDocument {

    @Id
    private String id;

    private String screenId;
    private String theatreId;

    private List<Section> sections;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section{
        private String sectionName; // "RECLINER", "PREMIUM", "REGULAR"
        private String seatType; // uniform for all seats in this section
        private BigDecimal basePrice;    // base price for this section
        private List<Row> rows;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row{
        private String label; // A, B, C...
        private List<SeatInfo> seats;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo{
        private Integer columnNumber;
        private Boolean isGap;
        private Boolean isBlocked;
    }
}
