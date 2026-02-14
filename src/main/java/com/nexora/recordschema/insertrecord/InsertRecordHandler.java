package com.nexora.recordschema.insertrecord;

import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.shared.command.CommandHandler;
import com.nexora.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class InsertRecordHandler implements CommandHandler<InsertRecordCommand, UUID> {

    private final RecordSchemaRepository schemaRepository;
    private final MongoTemplate mongoTemplate;
    private final EventPublisher eventPublisher;

    @Override
    public UUID handle(InsertRecordCommand command) {
        var schema = schemaRepository.findById(command.schemaId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Schema not found: " + command.schemaId()));

        if (command.agentAction() && !schema.getAgentOperations().createEnabled()) {
            throw new IllegalStateException(
                "Agent is not allowed to insert records into schema: " + command.schemaName());
        }

        var result = schema.handle(command);
        mongoTemplate.save(result.record(), command.schemaName());
        eventPublisher.publish(result.event());

        return result.record().getId();
    }

    @Override
    public Class<InsertRecordCommand> commandType() {
        return InsertRecordCommand.class;
    }
}
