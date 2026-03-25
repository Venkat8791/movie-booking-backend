package com.mxmovies.movie.controller;

import com.mxmovies.movie.dto.request.MovieRequest;
import com.mxmovies.movie.dto.response.MovieResponse;
import com.mxmovies.movie.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> createMovie(
            @Valid @RequestBody MovieRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movieService.createMovie(request));
    }

//    @GetMapping
//    public ResponseEntity<List<MovieResponse>> getAllMovies() {
//        return ResponseEntity.ok(movieService.getAllMovies());
//    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable UUID id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchByTitle(
            @RequestParam String title) {
        return ResponseEntity.ok(movieService.searchByTitle(title));
    }

    @GetMapping("/language/{language}")
    public ResponseEntity<List<MovieResponse>> getByLanguage(
            @PathVariable String language) {
        return ResponseEntity.ok(movieService.getByLanguage(language));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieResponse>> getByGenre(
            @PathVariable String genre) {
        return ResponseEntity.ok(movieService.getByGenre(genre));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable UUID id,
            @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getMovies(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String genre) {

        if (city != null) {
            return ResponseEntity.ok(movieService.getMoviesPlayingInCity(city));
        }
        if (title != null) {
            return ResponseEntity.ok(movieService.searchByTitle(title));
        }
        if (language != null) {
            return ResponseEntity.ok(movieService.getByLanguage(language));
        }
        if (genre != null) {
            return ResponseEntity.ok(movieService.getByGenre(genre));
        }
        return ResponseEntity.ok(movieService.getAllMovies());
    }
}
