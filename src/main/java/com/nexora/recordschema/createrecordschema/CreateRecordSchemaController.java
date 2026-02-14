package com.nexora.recordschema.createrecordschema;

import com.nexora.recordschema.shared.AgentOperations;
import com.nexora.recordschema.shared.ColumnDefinition;
import com.nexora.shared.command.CommandDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/record-schemas")
@RequiredArgsConstructor
class CreateRecordSchemaController {

    private final CommandDispatcher commandDispatcher;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CreateRecordSchemaResponse create(@RequestBody CreateRecordSchemaRequest request) {
        var command = new CreateRecordSchemaCommand(
            UUID.randomUUID(),
            Instant.now(),
            request.name(),
            request.columns(),
            request.agentOperations() != null ? request.agentOperations() : AgentOperations.allEnabled()
        );

        UUID schemaId = commandDispatcher.dispatch(command);

        return new CreateRecordSchemaResponse(schemaId);
    }

    record CreateRecordSchemaRequest(
        String name,
        List<ColumnDefinition> columns,
        AgentOperations agentOperations
    ) {
    }

    record CreateRecordSchemaResponse(UUID id) {
    }
}
