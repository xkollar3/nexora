package com.nexora.recordschema.customizeagent;

import com.nexora.shared.command.Command;

import java.time.Instant;
import java.util.UUID;

public record CustomizeAgentCommand(
    UUID commandId,
    Instant timestamp,
    UUID schemaId,
    String additionalContext
) implements Command {
}
