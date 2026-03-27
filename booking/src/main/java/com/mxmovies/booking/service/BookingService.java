package com.mxmovies.booking.service;

import com.mxmovies.booking.dto.request.BookingRequest;
import com.mxmovies.booking.dto.request.PaymentRequest;
import com.mxmovies.booking.dto.response.BookedSeatResponse;
import com.mxmovies.booking.dto.response.BookingResponse;
import com.mxmovies.booking.model.Booking;
import com.mxmovies.booking.model.BookingStatus;
import com.mxmovies.booking.model.PaymentStatus;
import com.mxmovies.booking.repository.BookingRepository;
import com.mxmovies.common.exception.BadRequestException;
import com.mxmovies.common.exception.ConflictException;
import com.mxmovies.common.exception.ResourceNotFoundException;
import com.mxmovies.common.exception.UnauthorizedException;
import com.mxmovies.show.model.Show;
import com.mxmovies.show.model.ShowSeat;
import com.mxmovies.show.model.ShowSeatStatus;
import com.mxmovies.show.repository.ShowRepository;
import com.mxmovies.show.repository.ShowSeatRepository;
import com.mxmovies.theatre.model.Seat;
import com.mxmovies.theatre.model.mongo.SeatLayoutDocument;
import com.mxmovies.theatre.repository.SeatLayoutRepository;
import com.mxmovies.theatre.repository.SeatRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {
    private static final int BOOKING_EXPIRY_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatRepository seatRepository;
    private final SeatLayoutRepository seatLayoutRepository;

    public BookingService(BookingRepository bookingRepository, ShowRepository showRepository, ShowSeatRepository showSeatRepository, SeatRepository seatRepository, SeatLayoutRepository seatLayoutRepository) {
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.seatRepository = seatRepository;
        this.seatLayoutRepository = seatLayoutRepository;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request, UUID userId){

        //validate show exists and is active
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(()-> new ResourceNotFoundException("Show not found"));

        if(!show.getStatus().name().equals("ACTIVE")){
            throw new ConflictException("Show is not active");
        }

        //validate all seats belongs to this shows's screen
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        if(seats.size()!=request.getSeatIds().size()){
            throw new ResourceNotFoundException("One or more seats not found");
        }

        //validate all seats belong to the shows's screen
        seats.forEach(seat -> {
            if(!seat.getScreen().getId().equals(show.getScreenId())){
                throw new ConflictException(
                        "Seat " + seat.getId() + " does not belong to this show's screen"
                );
            }
        });

        //fetch layout to get section base prices
        SeatLayoutDocument layout = seatLayoutRepository
                .findByScreenId(show.getScreenId().toString())
                .orElseThrow(() -> new ResourceNotFoundException("Seat layout not found"));

        //build row -> section base price map
        Map<String, BigDecimal> rowBasePriceMap = new HashMap<>();
        layout.getSections().forEach(section ->
                section.getRows().forEach(row->
                        rowBasePriceMap.put(row.getLabel(), section.getBasePrice())
                )
        );

        //calculate total amount
        BigDecimal totalAmount = seats.stream()
                .map(seat->{
                    BigDecimal basePrice = rowBasePriceMap.getOrDefault(seat.getRowLabel(), BigDecimal.ZERO);
                    return basePrice.multiply(show.getPriceMultiplier());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //create booking with PENDING status
        LocalDateTime now = LocalDateTime.now();

        Booking booking = Booking.builder()
                .userId(userId)
                .showId(request.getShowId())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .bookedAt(now)
                .expiresAt(now.plusMinutes(BOOKING_EXPIRY_MINUTES))
                .build();

        booking = bookingRepository.save(booking);
        final UUID bookingId = booking.getId();

        //try to clain seats in show_seats
        //UNIQUE(show_id, seat_id) constraint handles concurrency

        try{
            final Booking finalBooking = booking;
            List<ShowSeat> showSeats = seats.stream()
                    .map(seat -> {
                        BigDecimal basePrice = rowBasePriceMap
                                .getOrDefault(seat.getRowLabel(), BigDecimal.ZERO);
                        BigDecimal price = basePrice.multiply(show.getPriceMultiplier());

                        return ShowSeat.builder()
                                .show(show)
                                .seatId(seat.getId())
                                .bookingId(bookingId)
                                .status(ShowSeatStatus.PENDING)
                                .price(price)
                                .build();
                    })
                    .collect(Collectors.toList());

            showSeatRepository.saveAll(showSeats);
        }catch (DataIntegrityViolationException e){
            // one or more seats already taken
            // transaction will rollback automatically
            throw new DataIntegrityViolationException(
                    "One or more selected seats are no longer available"
            );
        }

        // build response
        return buildBookingResponse(booking, seats, show, rowBasePriceMap);
    }


    @Transactional
    public BookingResponse confirmPayment(UUID bookingId, PaymentRequest request, UUID userId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        //validate booking belongs to user
        if(!booking.getUserId().equals(userId)){
            throw new UnauthorizedException("Unauthorized");
        }

        //validate booking is still pending
        if(!booking.getStatus().equals(BookingStatus.PENDING)){
            throw new BadRequestException("Booking is no longer pending");
        }

        //validate booking not expired
        if (LocalDateTime.now().isAfter(booking.getExpiresAt())) {
            throw new BadRequestException(
                    "Booking has expired — please select seats again"
            );
        }

        // update show_seats to BOOKED
        List<ShowSeat> showSeats = showSeatRepository
                .findByBookingId(bookingId);

        showSeats.forEach(ss -> ss.setStatus(ShowSeatStatus.BOOKED));
        showSeatRepository.saveAll(showSeats);

        //confirm booking
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking = bookingRepository.save(booking);

        //build response
        List<UUID> seatIds = showSeats.stream()
                .map(ShowSeat::getSeatId)
                .collect(Collectors.toList());

        List<Seat> seats = seatRepository.findAllById(seatIds);

        Show show = showRepository.findById(booking.getShowId())
                .orElseThrow(()-> new ResourceNotFoundException("show not found"));

        SeatLayoutDocument layout = seatLayoutRepository
                .findByScreenId(show.getScreenId().toString())
                .orElseThrow(() -> new ResourceNotFoundException("Layout not found"));

        Map<String, BigDecimal> rowBasePriceMap = new HashMap<>();

        layout.getSections().forEach(section ->
                section.getRows().forEach(row ->
                        rowBasePriceMap.put(row.getLabel(), section.getBasePrice())
                )
        );

        return buildBookingResponse(booking, seats, show, rowBasePriceMap);
    }

    @Transactional
    public void cancelBooking(UUID bookingId, UUID userId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking already cancelled");
        }

        // release seats
        showSeatRepository.deleteByBookingId(bookingId);

        // cancel booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.REFUNDED);
        bookingRepository.save(booking);
    }

    public List<BookingResponse> getMyBookings(UUID userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(booking -> {
                    List<ShowSeat> showSeats = showSeatRepository
                            .findByBookingId(booking.getId());
                    List<UUID> seatIds = showSeats.stream()
                            .map(ShowSeat::getSeatId)
                            .collect(Collectors.toList());
                    List<Seat> seats = seatRepository.findAllById(seatIds);

                    Show show = showRepository.findById(booking.getShowId())
                            .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

                    var layout = seatLayoutRepository
                            .findByScreenId(show.getScreenId().toString())
                            .orElseThrow(() -> new ResourceNotFoundException("Layout not found"));

                    Map<String, BigDecimal> rowBasePriceMap = new HashMap<>();
                    layout.getSections().forEach(section ->
                            section.getRows().forEach(row ->
                                    rowBasePriceMap.put(
                                            row.getLabel(),
                                            section.getBasePrice()
                                    )
                            )
                    );

                    return buildBookingResponse(booking, seats, show, rowBasePriceMap);
                })
                .collect(Collectors.toList());
    }


    public BookingResponse getBookingById(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized");
        }

        List<ShowSeat> showSeats = showSeatRepository
                .findByBookingId(bookingId);
        List<UUID> seatIds = showSeats.stream()
                .map(ShowSeat::getSeatId)
                .collect(Collectors.toList());
        List<Seat> seats = seatRepository.findAllById(seatIds);

        Show show = showRepository.findById(booking.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        var layout = seatLayoutRepository
                .findByScreenId(show.getScreenId().toString())
                .orElseThrow(() -> new ResourceNotFoundException("Layout not found"));

        Map<String, BigDecimal> rowBasePriceMap = new HashMap<>();
        layout.getSections().forEach(section ->
                section.getRows().forEach(row ->
                        rowBasePriceMap.put(row.getLabel(), section.getBasePrice())
                )
        );

        return buildBookingResponse(booking, seats, show, rowBasePriceMap);
    }

    // ── Scheduled job — release expired PENDING bookings ─────────────────────
    @Scheduled(fixedRate = 60000) // runs every 60 seconds
    @Transactional
    public void releaseExpiredBookings() {
        List<Booking> expiredBookings = bookingRepository
                .findExpiredPendingBookings(LocalDateTime.now());

        if (expiredBookings.isEmpty()) return;

        log.info("Releasing {} expired bookings", expiredBookings.size());

        expiredBookings.forEach(booking -> {
            // release seats
            showSeatRepository.deleteByBookingId(booking.getId());

            // cancel booking
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        });
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private BookingResponse buildBookingResponse(Booking booking,
                                                 List<Seat> seats,
                                                 Show show,
                                                 Map<String, BigDecimal> rowBasePriceMap) {
        List<BookedSeatResponse> seatResponses = seats.stream()
                .map(seat -> {
                    BigDecimal basePrice = rowBasePriceMap
                            .getOrDefault(seat.getRowLabel(), BigDecimal.ZERO);
                    BigDecimal price = basePrice.multiply(show.getPriceMultiplier());

                    return BookedSeatResponse.builder()
                            .seatId(seat.getId())
                            .seatLabel(seat.getSeatLabel())
                            .seatType(seat.getSeatType().name())
                            .price(price)
                            .build();
                })
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .showId(booking.getShowId())
                .seats(seatResponses)
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus().name())
                .paymentStatus(booking.getPaymentStatus().name())
                .bookedAt(booking.getBookedAt())
                .expiresAt(booking.getExpiresAt())
                .build();
    }
}
