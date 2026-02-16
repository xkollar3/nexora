package com.nexora.recordschema.createrecordschema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nexora.recordschema.shared.AgentOperations;
import com.nexora.recordschema.shared.ColumnDefinition;
import com.nexora.shared.command.CommandDispatcher;
import com.nexora.shared.mcp.ToolProvider;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateRecordSchemaTools implements ToolProvider {

  private final CommandDispatcher commandDispatcher;

  @Tool(name = "create_record_schema", description = "Create a new record schema with a name, a list of column definitions (each with name, type, and required flag), and optional agent operations")
  public UUID createRecordSchema(String name, List<ColumnDefinition> columns, AgentOperations agentOperations) {
    log.debug("Calling create record schema tool: {}", name);

    var command = new CreateRecordSchemaCommand(
        UUID.randomUUID(),
        Instant.now(),
        name,
        columns,
        agentOperations != null ? agentOperations : AgentOperations.allEnabled());

    return commandDispatcher.dispatch(command);
  }
}
