package com.nexora.recordschema.customizeagent;

import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.shared.command.CommandDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/record-schemas")
@RequiredArgsConstructor
class CustomizeAgentController {

    private final CommandDispatcher commandDispatcher;
    private final RecordSchemaRepository repository;

    @PutMapping("/{schemaName}/agent")
    CustomizeAgentResponse customize(
        @PathVariable String schemaName,
        @RequestBody CustomizeAgentRequest request
    ) {
        var schema = repository.findByName(schemaName)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Schema not found: " + schemaName));

        var command = new CustomizeAgentCommand(
            UUID.randomUUID(),
            Instant.now(),
            schema.getId(),
            request.additionalContext()
        );

        UUID schemaId = commandDispatcher.dispatch(command);

        return new CustomizeAgentResponse(schemaId);
    }

    record CustomizeAgentRequest(String additionalContext) {
    }

    record CustomizeAgentResponse(UUID id) {
    }
}
