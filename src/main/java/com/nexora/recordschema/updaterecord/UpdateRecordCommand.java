package com.nexora.recordschema.updaterecord;

import com.nexora.shared.command.Command;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UpdateRecordCommand(
    UUID commandId,
    Instant timestamp,
    UUID schemaId,
    String schemaName,
    boolean agentAction,
    UUID recordId,
    Map<String, Object> fields
) implements Command {
}
