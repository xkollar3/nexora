package com.nexora.recordschema.customizeagent;

import com.nexora.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AgentCustomizedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID schemaId
) implements DomainEvent {
}
