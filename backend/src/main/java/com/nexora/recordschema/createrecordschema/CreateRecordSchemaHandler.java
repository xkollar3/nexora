package com.nexora.recordschema.createrecordschema;

import com.nexora.recordschema.shared.RecordSchema;
import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.shared.command.CommandHandler;
import com.nexora.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class CreateRecordSchemaHandler implements CommandHandler<CreateRecordSchemaCommand, UUID> {

    private final RecordSchemaRepository repository;
    private final EventPublisher eventPublisher;

    @Override
    public UUID handle(CreateRecordSchemaCommand command) {
        var schema = new RecordSchema();
        var event = schema.handle(command);
        repository.save(schema);
        eventPublisher.publish(event);
        return schema.getId();
    }

    @Override
    public Class<CreateRecordSchemaCommand> commandType() {
        return CreateRecordSchemaCommand.class;
    }
}
