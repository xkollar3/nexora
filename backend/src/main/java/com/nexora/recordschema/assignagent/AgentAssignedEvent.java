package com.nexora.recordschema.assignagent;

import com.nexora.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AgentAssignedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID schemaId
) implements DomainEvent {
}
