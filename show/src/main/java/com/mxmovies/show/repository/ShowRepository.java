package com.mxmovies.show.repository;

import com.mxmovies.show.model.Show;
import com.mxmovies.show.model.ShowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShowRepository extends JpaRepository<Show, UUID> {

    List<Show> findByMovieId(UUID movieId);

    List<Show> findByScreenId(UUID screenId);

    // get all active shows for a movie
    List<Show> findByMovieIdAndStatus(UUID movieId, ShowStatus status);

    // get all shows for a screen between two times
    // used to check screen availability before creating a new show
    List<Show> findByScreenIdAndShowTimeBetween(
            UUID screenId,
            LocalDateTime from,
            LocalDateTime to
    );
}
