package com.mxmovies.repository;

import com.mxmovies.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, UUID> {

    List<Theatre> findByCity(String city);

    List<Theatre> findByCityIgnoreCase(String city);
}
