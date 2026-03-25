package com.mxmovies.show.repository;

import com.mxmovies.show.model.Show;
import com.mxmovies.show.model.ShowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
    SELECT s FROM Show s
    WHERE s.movieId = :movieId
    AND s.screenId IN (
        SELECT sc.id FROM Screen sc
        WHERE sc.theatre.city = :city
    )
    AND s.status = 'ACTIVE'
    AND CAST(s.showTime AS date) = CAST(:date AS date)
    ORDER BY s.showTime ASC
""")
    List<Show> findByMovieAndCityAndDate(@Param("movieId") UUID movieId, @Param("city") String city, @Param("date") LocalDateTime date);
}
