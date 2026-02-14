package com.nexora.recordschema.insertrecord;

import com.nexora.shared.command.Command;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record InsertRecordCommand(
    UUID commandId,
    Instant timestamp,
    UUID schemaId,
    String schemaName,
    boolean agentAction,
    Map<String, Object> fields
) implements Command {
}
