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
