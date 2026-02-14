package com.nexora.recordschema.insertrecord;

import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.recordschema.shared.RecordValidationException;
import com.nexora.shared.command.CommandDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
class InsertRecordController {

    private final CommandDispatcher commandDispatcher;
    private final RecordSchemaRepository schemaRepository;

    @PostMapping("/{schemaName}")
    @ResponseStatus(HttpStatus.CREATED)
    InsertRecordResponse insert(
        @PathVariable String schemaName,
        @RequestBody Map<String, Object> fields
    ) {
        var schema = schemaRepository.findByName(schemaName)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Schema not found: " + schemaName));

        var command = new InsertRecordCommand(
            UUID.randomUUID(),
            Instant.now(),
            schema.getId(),
            schemaName,
            fields
        );

        try {
            UUID recordId = commandDispatcher.dispatch(command);
            return new InsertRecordResponse(recordId);
        } catch (RecordValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    record InsertRecordResponse(UUID id) {
    }
}
