package com.nexora.recordschema.updaterecord;

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
class UpdateRecordController {

    private final CommandDispatcher commandDispatcher;
    private final RecordSchemaRepository schemaRepository;

    @PatchMapping("/{schemaName}/{recordId}")
    @ResponseStatus(HttpStatus.OK)
    void update(
        @PathVariable String schemaName,
        @PathVariable UUID recordId,
        @RequestBody Map<String, Object> fields
    ) {
        var schema = schemaRepository.findByName(schemaName)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Schema not found: " + schemaName));

        var command = new UpdateRecordCommand(
            UUID.randomUUID(),
            Instant.now(),
            schema.getId(),
            schemaName,
            false,
            recordId,
            fields
        );

        try {
            commandDispatcher.dispatch(command);
        } catch (RecordValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
