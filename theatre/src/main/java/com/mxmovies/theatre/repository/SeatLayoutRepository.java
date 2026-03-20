package com.mxmovies.theatre.repository;

import com.mxmovies.theatre.model.mongo.SeatLayoutDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatLayoutRepository extends MongoRepository<SeatLayoutDocument,String> {
    Optional<SeatLayoutDocument> findByScreenId(UUID screenId);
}
