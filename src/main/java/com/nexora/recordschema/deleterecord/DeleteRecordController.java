package com.nexora.recordschema.deleterecord;

import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.shared.command.CommandDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
class DeleteRecordController {

    private final CommandDispatcher commandDispatcher;
    private final RecordSchemaRepository schemaRepository;

    @DeleteMapping("/{schemaName}/{recordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(
        @PathVariable String schemaName,
        @PathVariable UUID recordId
    ) {
        var schema = schemaRepository.findByName(schemaName)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Schema not found: " + schemaName));

        var command = new DeleteRecordCommand(
            UUID.randomUUID(),
            Instant.now(),
            schema.getId(),
            schemaName,
            false,
            recordId
        );

        try {
            commandDispatcher.dispatch(command);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
