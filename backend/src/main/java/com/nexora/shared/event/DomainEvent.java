package com.nexora.shared.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events.
 * Events represent something that has happened in the system.
 */
public interface DomainEvent {

    UUID eventId();

    Instant occurredAt();
}
