package com.mxmovies.repository;

import com.mxmovies.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    List<Movie> findByLanguage(String language);

    List<Movie> findByGenre(String genre);

    // search movies by title (case insensitive partial match)
    List<Movie> findByTitleContainingIgnoreCase(String title);
}
