package com.nexora.recordschema.updaterecord;

import com.nexora.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record RecordUpdatedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID recordId,
    UUID schemaId
) implements DomainEvent {
}
