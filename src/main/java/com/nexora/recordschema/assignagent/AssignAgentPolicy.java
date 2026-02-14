package com.nexora.recordschema.assignagent;

import com.nexora.recordschema.createrecordschema.RecordSchemaCreatedEvent;
import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
class AssignAgentPolicy {

    private final RecordSchemaRepository repository;
    private final SystemPromptGenerator promptGenerator;
    private final EventPublisher eventPublisher;

    @EventListener
    void on(RecordSchemaCreatedEvent event) {
        var schema = repository.findById(event.schemaId())
            .orElseThrow(() -> new IllegalStateException(
                "RecordSchema not found: " + event.schemaId()));

        var command = new AssignAgentCommand(
            UUID.randomUUID(),
            Instant.now(),
            event.schemaId(),
            promptGenerator.generate(schema)
        );

        var agentEvent = schema.handle(command);
        repository.save(schema);
        eventPublisher.publish(agentEvent);

        log.info("Agent assigned to schema '{}' ({})", event.schemaName(), event.schemaId());
    }
}
