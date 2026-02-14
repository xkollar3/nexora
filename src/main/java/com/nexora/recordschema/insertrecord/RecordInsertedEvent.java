package com.nexora.recordschema.insertrecord;

import com.nexora.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record RecordInsertedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID recordId,
    UUID schemaId
) implements DomainEvent {
}
