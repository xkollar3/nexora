package com.nexora.recordschema.deleterecord;

import com.nexora.recordschema.shared.Record;
import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.shared.command.CommandHandler;
import com.nexora.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class DeleteRecordHandler implements CommandHandler<DeleteRecordCommand, UUID> {

    private final RecordSchemaRepository schemaRepository;
    private final MongoTemplate mongoTemplate;
    private final EventPublisher eventPublisher;

    @Override
    public UUID handle(DeleteRecordCommand command) {
        var schema = schemaRepository.findById(command.schemaId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Schema not found: " + command.schemaId()));

        if (command.agentAction() && !schema.getAgentOperations().deleteEnabled()) {
            throw new IllegalStateException(
                "Agent is not allowed to delete records from schema: " + command.schemaName());
        }

        var query = new Query(Criteria.where("_id").is(command.recordId()));
        var result = mongoTemplate.remove(query, Record.class, command.schemaName());

        if (result.getDeletedCount() == 0) {
            throw new IllegalArgumentException("Record not found: " + command.recordId());
        }

        var event = new RecordDeletedEvent(
            UUID.randomUUID(),
            Instant.now(),
            command.recordId(),
            command.schemaId()
        );
        eventPublisher.publish(event);

        return command.recordId();
    }

    @Override
    public Class<DeleteRecordCommand> commandType() {
        return DeleteRecordCommand.class;
    }
}
