package com.nexora.recordschema.updaterecord;

import com.nexora.recordschema.shared.Record;
import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.shared.command.CommandHandler;
import com.nexora.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class UpdateRecordHandler implements CommandHandler<UpdateRecordCommand, UUID> {

    private final RecordSchemaRepository schemaRepository;
    private final MongoTemplate mongoTemplate;
    private final EventPublisher eventPublisher;

    @Override
    public UUID handle(UpdateRecordCommand command) {
        var schema = schemaRepository.findById(command.schemaId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Schema not found: " + command.schemaId()));

        var event = schema.handle(command);

        var query = new Query(Criteria.where("_id").is(command.recordId()));
        var existing = mongoTemplate.findOne(query, Record.class, command.schemaName());
        if (existing == null) {
            throw new IllegalArgumentException("Record not found: " + command.recordId());
        }

        var update = new Update();
        command.fields().forEach((key, value) -> update.set("fields." + key, value));
        mongoTemplate.updateFirst(query, update, Record.class, command.schemaName());

        eventPublisher.publish(event);

        return command.recordId();
    }

    @Override
    public Class<UpdateRecordCommand> commandType() {
        return UpdateRecordCommand.class;
    }
}
