package com.mxmovies.show.controller;

import com.mxmovies.show.dto.request.ShowRequest;
import com.mxmovies.show.dto.response.SeatAvailabilityResponse;
import com.mxmovies.show.dto.response.ShowResponse;
import com.mxmovies.show.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowResponse> createShow(
            @Valid @RequestBody ShowRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(showService.createShow(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowResponse> getShowById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(showService.getShowById(id));
    }

//    @GetMapping
//    public ResponseEntity<List<ShowResponse>> getShowsByMovie(
//            @RequestParam UUID movieId) {
//        return ResponseEntity.ok(showService.getShowsByMovie(movieId));
//    }


    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<ShowResponse>> getShowsByScreen(
            @PathVariable UUID screenId) {
        return ResponseEntity.ok(showService.getShowsByScreen(screenId));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<SeatAvailabilityResponse> getSeatAvailability(
            @PathVariable UUID id) {
        return ResponseEntity.ok(showService.getSeatAvailability(id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelShow(@PathVariable UUID id) {
        showService.cancelShow(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> getShows(
            @RequestParam(required = false) UUID movieId,
            @RequestParam(required = false) UUID screenId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (movieId != null && city != null) {
            LocalDate selectedDate = date != null ? date : LocalDate.now();
            return ResponseEntity.ok(
                    showService.getShowsGroupedByTheatre(movieId, city, selectedDate)
            );
        }
        if (movieId != null) {
            return ResponseEntity.ok(showService.getShowsByMovie(movieId));
        }
        if (screenId != null) {
            return ResponseEntity.ok(showService.getShowsByScreen(screenId));
        }
        return ResponseEntity.ok(List.of());
    }

}
