package com.mxmovies.theatre.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"screen_id", "row_label", "column_number"}
        )
)
public class Seat {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "row_label", nullable = false)
    private String rowLabel;

    @Column(name = "column_number", nullable = false)
    private Integer columnNumber;

    @Column(name = "seat_label", nullable = false)
    private String seatLabel;  // "A1", "A2", "A3" etc

    @Column(name = "seat_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatType seatType;

}
