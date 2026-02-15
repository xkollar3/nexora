package com.nexora.shared.websocket;

import java.time.Instant;
import java.util.UUID;

public record RecordChangeEvent(
    String type,
    UUID recordId,
    UUID schemaId,
    Instant occurredAt
) {
}
