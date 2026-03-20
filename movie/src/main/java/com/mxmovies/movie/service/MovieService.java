package com.mxmovies.movie.service;

import com.mxmovies.movie.dto.request.MovieRequest;
import com.mxmovies.movie.dto.response.MovieResponse;
import com.mxmovies.movie.model.Movie;
import com.mxmovies.movie.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .language(request.getLanguage())
                .genre(request.getGenre())
                .durationMinutes(request.getDurationMinutes())
                .posterUrl(request.getPosterUrl())
                .rating(request.getRating())
                .releaseDate(request.getReleaseDate())
                .build();

        return mapToResponse(movieRepository.save(movie));
    }

    public MovieResponse getMovieById(UUID id) {
        return movieRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
    }

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MovieResponse> searchByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MovieResponse> getByLanguage(String language) {
        return movieRepository.findByLanguage(language)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MovieResponse> getByGenre(String genre) {
        return movieRepository.findByGenre(genre)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MovieResponse updateMovie(UUID id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Movie updated = Movie.builder()
                .id(movie.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .language(request.getLanguage())
                .genre(request.getGenre())
                .durationMinutes(request.getDurationMinutes())
                .posterUrl(request.getPosterUrl())
                .rating(request.getRating())
                .releaseDate(request.getReleaseDate())
                .build();

        return mapToResponse(movieRepository.save(updated));
    }


    public void deleteMovie(UUID id) {
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Movie not found");
        }
        movieRepository.deleteById(id);
    }

    private MovieResponse mapToResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .language(movie.getLanguage())
                .genre(movie.getGenre())
                .durationMinutes(movie.getDurationMinutes())
                .posterUrl(movie.getPosterUrl())
                .rating(movie.getRating())
                .releaseDate(movie.getReleaseDate())
                .createdAt(movie.getCreatedAt())
                .build();
    }

}
