package com.mxmovies.booking.repository;

import com.mxmovies.booking.model.Booking;
import com.mxmovies.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    // get all bookings for a user (booking history)
    List<Booking> findByUserId(UUID userId);

    // get all bookings for a show
    List<Booking> findByShowId(UUID showId);

    List<Booking> findByUserIdAndStatus(UUID userId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.expiresAt < :now")
    List<Booking> findExpiredPendingBookings(@Param("now") LocalDateTime now);

}
