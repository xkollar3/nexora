package com.nexora.recordschema.insertrecord;

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
public class InsertRecordTools implements ToolProvider {

  private final RecordSchemaRepository schemaRepository;
  private final CommandDispatcher commandDispatcher;

  @Tool(name = "insert_record", description = "Insert record into a specific schemaName, specified by a map of fields for the record")
  public InsertRecordResult insertRecord(String schemaName, Map<String, Object> fields) {
    log.debug("Calling insert record tool: {}", schemaName);
    var schema = schemaRepository.findByName(schemaName)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Schema not found: " + schemaName));

    var command = new InsertRecordCommand(
        UUID.randomUUID(),
        Instant.now(),
        schema.getId(),
        schemaName,
        false,
        fields);

    UUID recordId = commandDispatcher.dispatch(command);

    return new InsertRecordResult(
        recordId,
        "Record inserted. Use get_agent_instructions(\"" + schemaName
            + "\") to learn about this schema's structure, validation rules, and allowed operations before further modifications.");
  }

  record InsertRecordResult(UUID recordId, String note) {}
}
