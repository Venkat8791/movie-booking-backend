package com.mxmovies.show.repository;

import com.mxmovies.show.model.ShowSeat;
import com.mxmovies.show.model.ShowSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, UUID> {

    // get all booked/pending seats for a show
    // absent rows = available seats
    List<ShowSeat> findByShowId(UUID showId);

    List<ShowSeat> findByShowIdAndStatus(UUID showId, ShowSeatStatus status);

    // check if a specific seat is taken for a show
    Optional<ShowSeat> findByShowIdAndSeatId(UUID showId, UUID seatId);

    // get all seats for a booking (for receipt)
    List<ShowSeat> findByBookingId(UUID bookingId);
}
