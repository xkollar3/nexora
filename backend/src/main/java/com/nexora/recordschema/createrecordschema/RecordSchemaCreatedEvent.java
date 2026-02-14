package com.nexora.recordschema.createrecordschema;

import com.nexora.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record RecordSchemaCreatedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID schemaId,
    String schemaName
) implements DomainEvent {
}
