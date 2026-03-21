package com.mxmovies.theatre.service;

import com.mxmovies.common.exception.ResourceNotFoundException;
import com.mxmovies.theatre.dto.request.TheatreRequest;
import com.mxmovies.theatre.dto.response.TheatreResponse;
import com.mxmovies.theatre.model.Theatre;
import com.mxmovies.theatre.repository.TheatreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;

    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    public TheatreResponse createTheatre(TheatreRequest request){
        Theatre theatre = Theatre.builder()
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .pincode(request.getPincode())
                .build();

        Theatre savedTheatre = theatreRepository.save(theatre);
        return mapToResponse(savedTheatre);
    }

    public List<TheatreResponse> getAllTheatres() {
        return theatreRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TheatreResponse getTheatreById(UUID id) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found"));
        return mapToResponse(theatre);
    }

    public List<TheatreResponse> getTheatresByCity(String city) {
        return theatreRepository.findByCityIgnoreCase(city)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TheatreResponse mapToResponse(Theatre theatre) {
        return TheatreResponse.builder()
                .id(theatre.getId())
                .name(theatre.getName())
                .city(theatre.getCity())
                .address(theatre.getAddress())
                .pincode(theatre.getPincode())
                .build();
    }

}
