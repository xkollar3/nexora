package com.nexora.recordschema.deleterecord;

import java.time.Instant;
import java.util.UUID;

import com.nexora.shared.mcp.ToolProvider;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.shared.command.CommandDispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteRecordTools implements ToolProvider {

  private final RecordSchemaRepository schemaRepository;
  private final CommandDispatcher commandDispatcher;

  @Tool(name = "delete_record", description = "Delete a record from a specific schema by providing the record ID")
  public void deleteRecord(String schemaName, UUID recordId) {
    log.debug("Calling delete_record tool: {} {}", schemaName, recordId);
    var schema = schemaRepository.findByName(schemaName)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Schema not found: " + schemaName));

    var command = new DeleteRecordCommand(
        UUID.randomUUID(),
        Instant.now(),
        schema.getId(),
        schemaName,
        true,
        recordId);

    commandDispatcher.dispatch(command);
  }
}
