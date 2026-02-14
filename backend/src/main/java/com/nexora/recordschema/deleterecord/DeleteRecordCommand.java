package com.nexora.recordschema.deleterecord;

import com.nexora.shared.command.Command;

import java.time.Instant;
import java.util.UUID;

public record DeleteRecordCommand(
    UUID commandId,
    Instant timestamp,
    UUID schemaId,
    String schemaName,
    boolean agentAction,
    UUID recordId
) implements Command {
}
