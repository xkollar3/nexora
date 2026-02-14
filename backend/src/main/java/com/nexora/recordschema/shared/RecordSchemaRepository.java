package com.nexora.recordschema.shared;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecordSchemaRepository extends MongoRepository<RecordSchema, UUID> {

    Optional<RecordSchema> findByName(String name);
}
