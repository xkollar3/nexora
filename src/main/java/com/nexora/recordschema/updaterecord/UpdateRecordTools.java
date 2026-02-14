package com.nexora.recordschema.updaterecord;

import java.time.Instant;
import java.util.Map;
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
public class UpdateRecordTools implements ToolProvider {

  private final RecordSchemaRepository schemaRepository;
  private final CommandDispatcher commandDispatcher;

  @Tool(name = "update_record", description = "Update an existing record in a specific schema by providing the record ID and a map of fields to update")
  public void updateRecord(String schemaName, UUID recordId, Map<String, Object> fields) {
    log.debug("Calling update_record tool: {} {}", schemaName, recordId);
    var schema = schemaRepository.findByName(schemaName)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Schema not found: " + schemaName));

    var command = new UpdateRecordCommand(
        UUID.randomUUID(),
        Instant.now(),
        schema.getId(),
        schemaName,
        true,
        recordId,
        fields);

    commandDispatcher.dispatch(command);
  }
}
