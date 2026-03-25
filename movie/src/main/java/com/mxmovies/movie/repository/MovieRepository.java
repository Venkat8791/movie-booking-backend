package com.mxmovies.movie.repository;

import com.mxmovies.movie.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    List<Movie> findByLanguage(String language);

    List<Movie> findByGenre(String genre);

    // search movies by title (case insensitive partial match)
    List<Movie> findByTitleContainingIgnoreCase(String title);

    @Query("""
    SELECT DISTINCT m FROM Movie m
    WHERE m.id IN (
        SELECT s.movieId FROM Show s
        WHERE s.screenId IN (
            SELECT sc.id FROM Screen sc
            WHERE LOWER(sc.theatre.city) = LOWER(:city)
        )
        AND s.status = 'ACTIVE'
        AND s.showTime > :now
    )
""")
    List<Movie> findMoviesPlayingInCity(
            @Param("city") String city,
            @Param("now") LocalDateTime now
    );
}
