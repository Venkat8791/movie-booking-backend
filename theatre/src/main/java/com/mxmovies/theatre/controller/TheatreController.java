package com.mxmovies.theatre.controller;

import com.mxmovies.theatre.dto.request.ScreenRequest;
import com.mxmovies.theatre.dto.request.TheatreRequest;
import com.mxmovies.theatre.dto.response.ScreenResponse;
import com.mxmovies.theatre.dto.response.SeatResponse;
import com.mxmovies.theatre.dto.response.TheatreResponse;
import com.mxmovies.theatre.model.mongo.SeatLayoutDocument;
import com.mxmovies.theatre.service.ScreenService;
import com.mxmovies.theatre.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    private final TheatreService theatreService;
    private final ScreenService screenService;


    public TheatreController(TheatreService theatreService, ScreenService screenService) {
        this.theatreService = theatreService;
        this.screenService = screenService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheatreResponse> createTheatre(@Valid @RequestBody TheatreRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(theatreService.createTheatre(request));
    }

    @GetMapping
    public ResponseEntity<List<TheatreResponse>> getAllTheatres(){
        return ResponseEntity.ok(theatreService.getAllTheatres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TheatreResponse> getTheatreById(@PathVariable UUID id){
        return ResponseEntity.ok(theatreService.getTheatreById(id));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<TheatreResponse>> getTheatresByCity(@PathVariable String city){
        return ResponseEntity.ok(theatreService.getTheatresByCity(city));
    }

    // --- screen endpoints
    @PostMapping("/{theatreId}/screens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreenResponse> createScreen(
            @PathVariable UUID theatreId,
            @Valid @RequestBody ScreenRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(screenService.createScreen(theatreId, request));
    }

    @GetMapping("/{theatreId}/screens")
    public ResponseEntity<List<ScreenResponse>> getScreensByTheatre(
            @PathVariable UUID theatreId) {
        return ResponseEntity.ok(screenService.getScreensByTheatre(theatreId));
    }

    @GetMapping("/{theatreId}/screens/{screenId}")
    public ResponseEntity<ScreenResponse> getScreenById(
            @PathVariable UUID theatreId,
            @PathVariable UUID screenId) {
        return ResponseEntity.ok(screenService.getScreenById(screenId));
    }


    //---seats

    @PostMapping("/{theatreId}/screens/{screenId}/layout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> uploadSeatLayout(
            @PathVariable UUID theatreId,
            @PathVariable UUID screenId,
            @RequestParam("file") MultipartFile file) {
        // validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".json")) {
            throw new RuntimeException("Only JSON files are accepted");
        }

        screenService.uploadSeatLayout(theatreId,screenId, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{theatreId}/screens/{screenId}/layout")
    public ResponseEntity<SeatLayoutDocument> getSeatLayout(
            @PathVariable UUID theatreId,
            @PathVariable UUID screenId) {
        return ResponseEntity.ok(screenService.getSeatLayout(screenId));
    }


    @GetMapping("/{theatreId}/screens/{screenId}/seats")
    public ResponseEntity<List<SeatResponse>> getSeatsByScreen(
            @PathVariable UUID theatreId,
            @PathVariable UUID screenId) {
        return ResponseEntity.ok(screenService.getSeatsByScreen(screenId));
    }






}
