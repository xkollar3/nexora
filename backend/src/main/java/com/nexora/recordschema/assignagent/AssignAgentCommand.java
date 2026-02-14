package com.nexora.recordschema.assignagent;

import com.nexora.shared.command.Command;

import java.time.Instant;
import java.util.UUID;

public record AssignAgentCommand(
    UUID commandId,
    Instant timestamp,
    UUID schemaId,
    String systemPrompt
) implements Command {
}
