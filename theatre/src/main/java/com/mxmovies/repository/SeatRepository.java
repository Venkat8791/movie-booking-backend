package com.mxmovies.repository;

import com.mxmovies.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByScreenId(UUID screenId);

    // useful for seat selection UI — get all seats for a screen
    // ordered by row and column
    List<Seat> findByScreenIdOrderByRowLabelAscColumnNumberAsc(UUID screenId);
}
