package com.nexora.recordschema.customizeagent;

import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.shared.command.CommandHandler;
import com.nexora.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class CustomizeAgentHandler implements CommandHandler<CustomizeAgentCommand, UUID> {

    private final RecordSchemaRepository repository;
    private final EventPublisher eventPublisher;

    @Override
    public UUID handle(CustomizeAgentCommand command) {
        var schema = repository.findById(command.schemaId())
            .orElseThrow(() -> new IllegalArgumentException(
                "RecordSchema not found: " + command.schemaId()));

        var event = schema.handle(command);
        repository.save(schema);
        eventPublisher.publish(event);

        return schema.getId();
    }

    @Override
    public Class<CustomizeAgentCommand> commandType() {
        return CustomizeAgentCommand.class;
    }
}
