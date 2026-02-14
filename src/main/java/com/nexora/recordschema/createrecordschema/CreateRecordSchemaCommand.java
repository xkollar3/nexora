package com.nexora.recordschema.createrecordschema;

import com.nexora.recordschema.shared.AgentOperations;
import com.nexora.recordschema.shared.ColumnDefinition;
import com.nexora.shared.command.Command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateRecordSchemaCommand(
    UUID commandId,
    Instant timestamp,
    String name,
    List<ColumnDefinition> columns,
    AgentOperations agentOperations
) implements Command {
}
