package com.nexora.recordschema.deleterecord;

import com.nexora.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record RecordDeletedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID recordId,
    UUID schemaId
) implements DomainEvent {
}
