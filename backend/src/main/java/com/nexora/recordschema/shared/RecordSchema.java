package com.nexora.recordschema.shared;

import com.nexora.recordschema.assignagent.AgentAssignedEvent;
import com.nexora.recordschema.assignagent.AssignAgentCommand;
import com.nexora.recordschema.createrecordschema.CreateRecordSchemaCommand;
import com.nexora.recordschema.createrecordschema.RecordSchemaCreatedEvent;
import com.nexora.recordschema.customizeagent.AgentCustomizedEvent;
import com.nexora.recordschema.customizeagent.CustomizeAgentCommand;
import com.nexora.recordschema.insertrecord.InsertRecordCommand;
import com.nexora.recordschema.insertrecord.InsertRecordResult;
import com.nexora.recordschema.insertrecord.RecordInsertedEvent;
import com.nexora.recordschema.updaterecord.RecordUpdatedEvent;
import com.nexora.recordschema.updaterecord.UpdateRecordCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Document(collection = "record_schemas")
@Getter
@NoArgsConstructor
public class RecordSchema {

    @Id
    private UUID id;
    private String name;
    private List<ColumnDefinition> columns;
    private AgentOperations agentOperations;
    private String agentSystemPrompt;
    private String additionalContext;

    public RecordSchemaCreatedEvent handle(CreateRecordSchemaCommand command) {
        this.id = UUID.randomUUID();
        this.name = command.name();
        this.columns = command.columns();
        this.agentOperations = command.agentOperations();

        return new RecordSchemaCreatedEvent(
            UUID.randomUUID(),
            Instant.now(),
            this.id,
            this.name
        );
    }

    public AgentAssignedEvent handle(AssignAgentCommand command) {
        this.agentSystemPrompt = command.systemPrompt();

        return new AgentAssignedEvent(
            UUID.randomUUID(),
            Instant.now(),
            this.id
        );
    }

    public AgentCustomizedEvent handle(CustomizeAgentCommand command) {
        this.additionalContext = command.additionalContext();

        return new AgentCustomizedEvent(
            UUID.randomUUID(),
            Instant.now(),
            this.id
        );
    }

    public InsertRecordResult handle(InsertRecordCommand command) {
        validateFields(command.fields());

        var record = new Record(UUID.randomUUID(), command.fields());
        var event = new RecordInsertedEvent(
            UUID.randomUUID(),
            Instant.now(),
            record.getId(),
            this.id
        );

        return new InsertRecordResult(record, event);
    }

    public RecordUpdatedEvent handle(UpdateRecordCommand command) {
        validateUpdateFields(command.fields());

        return new RecordUpdatedEvent(
            UUID.randomUUID(),
            Instant.now(),
            command.recordId(),
            this.id
        );
    }

    private void validateUpdateFields(Map<String, Object> fields) {
        var errors = new ArrayList<String>();

        var columnsByName = columns.stream()
            .collect(Collectors.toMap(ColumnDefinition::name, col -> col));

        for (var entry : fields.entrySet()) {
            var column = columnsByName.get(entry.getKey());
            if (column == null) {
                errors.add("Unknown column: " + entry.getKey());
                continue;
            }
            if (entry.getValue() != null && !isCompatibleType(column.type(), entry.getValue())) {
                errors.add("Invalid value for column '%s': expected %s".formatted(
                    entry.getKey(), column.type()));
            }
        }

        if (!errors.isEmpty()) {
            throw new RecordValidationException(errors);
        }
    }

    private boolean isCompatibleType(ColumnType type, Object value) {
        return switch (type) {
            case TEXT, DATE, REFERENCE -> value instanceof String;
            case NUMBER -> value instanceof Number;
            case BOOLEAN -> value instanceof Boolean;
        };
    }

    private void validateFields(Map<String, Object> fields) {
        var errors = new ArrayList<String>();

        var columnsByName = columns.stream()
            .collect(Collectors.toMap(ColumnDefinition::name, col -> col));

        for (var key : fields.keySet()) {
            if (!columnsByName.containsKey(key)) {
                errors.add("Unknown column: " + key);
            }
        }

        for (var col : columns) {
            if (col.required() && !fields.containsKey(col.name())) {
                errors.add("Missing required column: " + col.name());
            }
        }

        if (!errors.isEmpty()) {
            throw new RecordValidationException(errors);
        }
    }
}
