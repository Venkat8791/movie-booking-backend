package com.mxmovies.repository;

import com.mxmovies.model.Booking;
import com.mxmovies.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    // get all bookings for a user (booking history)
    List<Booking> findByUserId(UUID userId);

    // get all bookings for a show
    List<Booking> findByShowId(UUID showId);

    List<Booking> findByUserIdAndStatus(UUID userId, BookingStatus status);
}
