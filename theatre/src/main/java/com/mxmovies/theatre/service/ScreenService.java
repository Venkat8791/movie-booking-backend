package com.mxmovies.theatre.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxmovies.theatre.dto.request.ScreenRequest;
import com.mxmovies.theatre.dto.response.ScreenResponse;
import com.mxmovies.theatre.dto.response.SeatResponse;
import com.mxmovies.theatre.model.Screen;
import com.mxmovies.theatre.model.Seat;
import com.mxmovies.theatre.model.SeatType;
import com.mxmovies.theatre.model.Theatre;
import com.mxmovies.theatre.model.mongo.SeatLayoutDocument;
import com.mxmovies.theatre.repository.ScreenRepository;
import com.mxmovies.theatre.repository.SeatLayoutRepository;
import com.mxmovies.theatre.repository.SeatRepository;
import com.mxmovies.theatre.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;
    private final SeatRepository seatRepository;
    private final SeatLayoutRepository seatLayoutRepository;

    public ScreenService(ScreenRepository screenRepository, TheatreRepository theatreRepository, SeatRepository seatRepository, SeatLayoutRepository seatLayoutRepository) {
        this.screenRepository = screenRepository;
        this.theatreRepository = theatreRepository;
        this.seatRepository = seatRepository;
        this.seatLayoutRepository = seatLayoutRepository;
    }

    public ScreenResponse createScreen(UUID theatreId, ScreenRequest request){
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(()->new RuntimeException("Theatre not found"));

        Screen screen = Screen.builder()
                .theatre(theatre)
                .name(request.getName())
                .screenType(request.getScreenType())
                .totalRows(request.getTotalRows())
                .totalColumns(request.getTotalColumns())
                .build();

        Screen savedTheatre = screenRepository.save(screen);
        return mapToResponse(savedTheatre);
    }

    public List<ScreenResponse> getScreensByTheatre(UUID theatreId) {
        if (!theatreRepository.existsById(theatreId)) {
            throw new RuntimeException("Theatre not found");
        }
        return screenRepository.findByTheatreId(theatreId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ScreenResponse getScreenById(UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new RuntimeException("Screen not found"));
        return mapToResponse(screen);
    }


    public List<SeatResponse> getSeatsByScreen(UUID screenId) {
        if (!screenRepository.existsById(screenId)) {
            throw new RuntimeException("Screen not found");
        }
        return seatRepository
                .findByScreenIdOrderByRowLabelAscColumnNumberAsc(screenId)
                .stream()
                .map(this::mapSeatToResponse)
                .collect(Collectors.toList());
    }

    public void uploadSeatLayout(UUID theatreId, UUID screenId, MultipartFile file){
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(()-> new RuntimeException("Screen not found"));

        // validate screen actually belongs to the given theatre
        if (!screen.getTheatre().getId().equals(theatreId)) {
            throw new RuntimeException("Screen does not belong to this theatre");
        }

        try{
            // parse the uploaded JSON file into SeatLayoutDocument
            ObjectMapper objectMapper = new ObjectMapper();
            SeatLayoutDocument layoutRequest = objectMapper.readValue(
                    file.getInputStream(),
                    SeatLayoutDocument.class
            );

            // save to MongoDB
            SeatLayoutDocument document = SeatLayoutDocument.builder()
                    .screenId(screenId.toString())
                    .theatreId(screen.getTheatre().getId().toString())
                    .sections(layoutRequest.getSections())
                    .build();

            seatLayoutRepository.save(document);

            List<Seat> seats = document.getSections().stream()
                    .flatMap(section -> section.getRows().stream()
                            .flatMap(row -> row.getSeats().stream()
                                    .filter(s->!s.getIsGap() && !s.getIsBlocked())
                                    .map(s->Seat.builder()
                                            .screen(screen)
                                            .rowLabel(row.getLabel())
                                            .columnNumber(s.getColumnNumber())
                                            .seatType(SeatType.valueOf(section.getSeatType()))
                                            .build())))
                    .collect(Collectors.toList());

            seatRepository.saveAll(seats);

        }catch (IOException e) {
            throw new RuntimeException("Failed to parse layout file: " + e.getMessage());
        }
    }

    // get layout from MongoDB for UI rendering
    public SeatLayoutDocument getSeatLayout(UUID screenId) {
        return seatLayoutRepository.findByScreenId(screenId.toString())
                .orElseThrow(() -> new RuntimeException("Seat layout not found"));
    }

    private ScreenResponse mapToResponse(Screen screen) {
        return ScreenResponse.builder()
                .id(screen.getId())
                .theatreId(screen.getTheatre().getId())
                .name(screen.getName())
                .screenType(screen.getScreenType())
                .totalRows(screen.getTotalRows())
                .totalColumns(screen.getTotalColumns())
                .build();
    }

    private SeatResponse mapSeatToResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .screenId(seat.getScreen().getId())
                .rowLabel(seat.getRowLabel())
                .columnNumber(seat.getColumnNumber())
                .seatType(seat.getSeatType().name())
                .build();
    }

}
