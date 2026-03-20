package com.mxmovies.booking.controller;

import com.mxmovies.booking.dto.request.BookingRequest;
import com.mxmovies.booking.dto.request.PaymentRequest;
import com.mxmovies.booking.dto.response.BookingResponse;
import com.mxmovies.booking.service.BookingService;
import com.mxmovies.common.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final SecurityUtils securityUtils;

    public BookingController(BookingService bookingService, SecurityUtils securityUtils) {
        this.bookingService = bookingService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, userId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(bookingService.getMyBookings(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(bookingService.getBookingById(id, userId));
    }

    @PostMapping("/{id}/confirm-payment")
    public ResponseEntity<BookingResponse> confirmPayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(bookingService.confirmPayment(id, request, userId));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = securityUtils.getCurrentUserId();
        bookingService.cancelBooking(id, userId);
        return ResponseEntity.noContent().build();
    }


}
