package com.nexora.recordschema.assignagent;

import com.nexora.recordschema.shared.AgentOperations;
import com.nexora.recordschema.shared.ColumnDefinition;
import com.nexora.recordschema.shared.RecordSchema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class SystemPromptGenerator {

    String generate(RecordSchema schema) {
        var sb = new StringBuilder();

        sb.append("You are an agent managing records in the \"%s\" schema.\n\n".formatted(schema.getName()));

        sb.append("## Schema Structure\n\n");
        sb.append("Columns:\n");
        for (ColumnDefinition col : schema.getColumns()) {
            sb.append("- %s (%s, %s)\n".formatted(
                col.name(),
                col.type().name().toLowerCase(),
                col.required() ? "required" : "optional"
            ));
        }

        sb.append("\n## Allowed Operations\n\n");
        var operations = describeOperations(schema.getAgentOperations());
        if (operations.isEmpty()) {
            sb.append("You have read-only access. You may only query existing records.\n");
        } else {
            sb.append("You may perform the following operations:\n");
            operations.forEach(op -> sb.append("- %s\n".formatted(op)));
            sb.append("\n");
        }

        sb.append("You may always query and search existing records.\n");
        sb.append("Always validate required fields before creating or updating records.\n");

        sb.append("\n## Querying Records with Filters\n\n");
        sb.append("The `list_records` tool accepts a `filter` parameter as a MongoDB query JSON string.\n");
        sb.append("Record data is stored under a `fields` map, so all column names must be prefixed with `fields.` in filters.\n\n");
        sb.append("Examples for this schema:\n");

        var firstCol = schema.getColumns().get(0);
        var colRef = "fields." + firstCol.name();
        switch (firstCol.type()) {
            case TEXT -> {
                sb.append("- Exact match: `{\"%s\": \"some value\"}`\n".formatted(colRef));
                sb.append("- Pattern match: `{\"%s\": {\"$regex\": \"^prefix\", \"$options\": \"i\"}}`\n".formatted(colRef));
            }
            case NUMBER -> {
                sb.append("- Exact match: `{\"%s\": 42}`\n".formatted(colRef));
                sb.append("- Comparison: `{\"%s\": {\"$gt\": 50}}`\n".formatted(colRef));
            }
            case BOOLEAN -> sb.append("- Exact match: `{\"%s\": true}`\n".formatted(colRef));
            case DATE -> sb.append("- Exact match: `{\"%s\": \"2025-01-15\"}`\n".formatted(colRef));
            case REFERENCE -> sb.append("- Exact match: `{\"%s\": \"ref-id\"}`\n".formatted(colRef));
        }

        sb.append("- Multiple conditions (AND): combine fields, e.g. `{\"fields.col1\": \"val\", \"fields.col2\": 10}`\n");
        sb.append("- OR: `{\"$or\": [{\"fields.col1\": \"a\"}, {\"fields.col1\": \"b\"}]}`\n");
        sb.append("- Not equal: `{\"%s\": {\"$ne\": \"value\"}}`\n".formatted(colRef));
        sb.append("- In list: `{\"%s\": {\"$in\": [\"a\", \"b\"]}}`\n".formatted(colRef));
        sb.append("\nPass an empty object `{}` or omit the filter to return all records.\n");

        return sb.toString();
    }

    private List<String> describeOperations(AgentOperations ops) {
        var result = new ArrayList<String>();
        if (ops.createEnabled()) result.add("Create new records");
        if (ops.updateEnabled()) result.add("Update existing records");
        if (ops.deleteEnabled()) result.add("Delete records");
        return result;
    }
}
