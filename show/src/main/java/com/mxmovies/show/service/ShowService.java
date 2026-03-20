package com.mxmovies.show.service;

import com.mxmovies.movie.model.Movie;
import com.mxmovies.movie.repository.MovieRepository;
import com.mxmovies.show.dto.request.ShowRequest;
import com.mxmovies.show.dto.response.SeatAvailabilityResponse;
import com.mxmovies.show.dto.response.ShowResponse;
import com.mxmovies.show.model.Show;
import com.mxmovies.show.model.ShowSeat;
import com.mxmovies.show.model.ShowStatus;
import com.mxmovies.show.repository.ShowRepository;
import com.mxmovies.show.repository.ShowSeatRepository;
import com.mxmovies.theatre.model.Screen;
import com.mxmovies.theatre.model.Seat;
import com.mxmovies.theatre.model.mongo.SeatLayoutDocument;
import com.mxmovies.theatre.repository.ScreenRepository;
import com.mxmovies.theatre.repository.SeatLayoutRepository;
import com.mxmovies.theatre.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final SeatLayoutRepository seatLayoutRepository;


    public ShowService(ShowRepository showRepository, ShowSeatRepository showSeatRepository, MovieRepository movieRepository, ScreenRepository screenRepository, SeatRepository seatRepository, SeatLayoutRepository seatLayoutRepository) {
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.seatLayoutRepository = seatLayoutRepository;
    }

    public ShowResponse createShow(ShowRequest request){
        // validate movie exists
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(()->new RuntimeException("Movie not found"));

        // validate screen exists
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new RuntimeException("Screen not found"));

        // check screen is not already booked for this time slot
        LocalDateTime endTime = request.getShowTime()
                .plusMinutes(movie.getDurationMinutes());

        List<Show> overlappingShows = showRepository.findByScreenIdAndShowTimeBetween(
                request.getScreenId(),
                request.getShowTime().minusMinutes(movie.getDurationMinutes()),
                endTime
        );

        if(!overlappingShows.isEmpty()){
            throw new RuntimeException("Screen is already booked for this time slot");
        }

        Show show = Show.builder()
                .movieId(request.getMovieId())
                .screenId(request.getScreenId())
                .showTime(request.getShowTime())
                .priceMultiplier(request.getPriceMultiplier())
                .status(ShowStatus.ACTIVE)
                .build();

        return mapToResponse(showRepository.save(show));
    }

    public ShowResponse getShowById(UUID id){
        return showRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(()-> new RuntimeException("Show not found"));
    }

    public List<ShowResponse> getShowsByMovie(UUID movieId){
        return showRepository.findByMovieIdAndStatus(movieId, ShowStatus.ACTIVE)
                .stream()
                .map(this:: mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ShowResponse> getShowsByScreen(UUID screenId){
        return showRepository.findByScreenId(screenId)
                .stream()
                .map(this:: mapToResponse)
                .collect(Collectors.toList());
    }

    public SeatAvailabilityResponse getSeatAvailability(UUID showId){
        //get show
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found"));

        //get layout document from mongo
        SeatLayoutDocument layout = seatLayoutRepository
                .findByScreenId(show.getScreenId().toString())
                .orElseThrow(()-> new RuntimeException("Seat layout not found"));

        //get all seats for this screen from postgres
        List<Seat> allSeats = seatRepository.findByScreenIdOrderByRowLabelAscColumnNumberAsc(show.getScreenId());

        //build lookup map -> key: rowLabel_columNumber, value: seat
        Map<String, Seat> seatMap = allSeats.stream()
                .collect(Collectors.toMap(
                        s-> s.getRowLabel() + "_" + s.getColumnNumber(),
                        s->s
                ));

        //get taken seats for this show from PostgreSQL
        List<ShowSeat> takenSeats = showSeatRepository.findByShowId(showId);

        //build taken seat id set
        Map<UUID, String> takenSeatStatusMap = takenSeats.stream()
                .collect(Collectors.toMap(
                        ShowSeat:: getSeatId,
                        ss -> ss.getStatus().name()
                ));

        //enrich layout with availability
        List<SeatAvailabilityResponse.SectionAvailability> sections =
                layout.getSections().stream()
                        .map(section ->{
                            BigDecimal finalPrice = section.getBasePrice().multiply(show.getPriceMultiplier());

                            List<SeatAvailabilityResponse.RowAvailability> rows =
                                    section.getRows().stream()
                                            .map(row ->{
                                                List<SeatAvailabilityResponse.SeatAvailability> seats =
                                                        row.getSeats().stream()
                                                                .map(seatInfo -> {
                                                                    if(seatInfo.getIsGap()){
                                                                        return SeatAvailabilityResponse.SeatAvailability.builder()
                                                                                .seatId(null)
                                                                                .columnNumber(seatInfo.getColumnNumber())
                                                                                .isGap(true)
                                                                                .isBlocked(false)
                                                                                .status(null)
                                                                                .build();
                                                                    }
                                                                    if(seatInfo.getIsBlocked()){
                                                                        return SeatAvailabilityResponse.SeatAvailability.builder()
                                                                                .seatId(null)
                                                                                .columnNumber(seatInfo.getColumnNumber())
                                                                                .isGap(false)
                                                                                .isBlocked(true)
                                                                                .status("BLOCKED")
                                                                                .build();
                                                                    }

                                                                    //real seat - look up in map
                                                                    String key = row.getLabel() + "_" + seatInfo.getColumnNumber();
                                                                    Seat seat = seatMap.get(key);

                                                                    if(seat == null){
                                                                        // seat exists in layout but not in DB
                                                                        // shouldn't happen but handle gracefully
                                                                        return SeatAvailabilityResponse.SeatAvailability.builder()
                                                                                .seatId(null)
                                                                                .columnNumber(seatInfo.getColumnNumber())
                                                                                .isGap(false)
                                                                                .isBlocked(true)
                                                                                .status("AVAILABLE")
                                                                                .build();
                                                                    }

                                                                    //determine status
                                                                    String status = takenSeatStatusMap.getOrDefault(seat.getId(), "AVAILABLE");

                                                                    return SeatAvailabilityResponse.SeatAvailability.builder()
                                                                            .seatId(seat.getId())
                                                                            .columnNumber(seatInfo.getColumnNumber())
                                                                            .isGap(false)
                                                                            .isBlocked(false)
                                                                            .status(status)
                                                                            .build();

                                                                })
                                                                .collect(Collectors.toList());
                                                return SeatAvailabilityResponse.RowAvailability.builder()
                                                        .label(row.getLabel())
                                                        .seats(seats)
                                                        .build();
                                            })
                                            .collect(Collectors.toList());
                            return SeatAvailabilityResponse.SectionAvailability.builder()
                                    .sectionName(section.getSectionName())
                                    .seatType(section.getSeatType())
                                    .basePrice(section.getBasePrice())
                                    .finalPrice(finalPrice)
                                    .rows(rows)
                                    .build();
                        })
                        .collect(Collectors.toList());

        return SeatAvailabilityResponse.builder()
                .showId(showId)
                .sections(sections)
                .build();
    }

    public void cancelShow(UUID id){
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Show not found"));

        show = Show.builder()
                .id(show.getId())
                .movieId(show.getMovieId())
                .screenId(show.getScreenId())
                .showTime(show.getShowTime())
                .priceMultiplier(show.getPriceMultiplier())
                .status(ShowStatus.CANCELLED)
                .createdAt(show.getCreatedAt())
                .build();
        showRepository.save(show);
    }



    private ShowResponse mapToResponse(Show show){
        return ShowResponse.builder()
                .id(show.getId())
                .movieId((show.getMovieId()))
                .screenId(show.getScreenId())
                .showTime(show.getShowTime())
                .priceMultiplier(show.getPriceMultiplier())
                .status(show.getStatus().name())
                .createdAt(show.getCreatedAt())
                .build();
    }
}
