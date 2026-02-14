package com.nexora.recordschema.query;

import java.util.List;
import java.util.Map;

import com.nexora.recordschema.shared.RecordSchema;
import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.recordschema.shared.Record;
import com.nexora.shared.mcp.ToolProvider;
import com.nexora.shared.query.QueryDispatcher;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordSchemaQueryTools implements ToolProvider {

    private final QueryDispatcher queryDispatcher;
    private final RecordSchemaRepository schemaRepository;

    @Tool(name = "list_schemas", description = "List all record schemas with their names, column definitions, and agent operations")
    public List<SchemaInfo> listSchemas() {
        log.debug("Calling list_schemas tool");
        List<RecordSchema> schemas = queryDispatcher.dispatch(new ListSchemasQuery());
        return schemas.stream().map(SchemaInfo::from).toList();
    }

    @Tool(name = "list_records", description = "List records from a specific schema with pagination and optional MongoDB filter")
    public PaginatedRecords listRecords(
            @ToolParam(description = "The name of the schema to query") String schemaName,
            @ToolParam(description = "Page number (zero-based)", required = false) Integer page,
            @ToolParam(description = "Page size", required = false) Integer size,
            @ToolParam(description = "MongoDB filter as JSON string, e.g. {\"status\": \"active\"}", required = false) String filter) {
        log.debug("Calling list_records tool for schema: {}", schemaName);
        var query = new ListRecordsQuery(
                schemaName,
                page != null ? page : 0,
                size != null ? size : 20,
                filter != null ? filter : "{}");
        org.springframework.data.domain.Page<Record> result = queryDispatcher.dispatch(query);
        return new PaginatedRecords(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Tool(name = "get_agent_instructions", description = "Get the agent system prompt and additional context configured for a specific schema")
    public AgentInstructions getAgentInstructions(
            @ToolParam(description = "The name of the schema") String schemaName) {
        log.debug("Calling get_agent_instructions tool for schema: {}", schemaName);
        var schema = schemaRepository.findByName(schemaName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Schema not found: " + schemaName));
        return new AgentInstructions(schema.getName(), schema.getAgentSystemPrompt(), schema.getAdditionalContext());
    }

    record AgentInstructions(String schemaName, String systemPrompt, String additionalContext) {}

    record SchemaInfo(
            String name,
            List<ColumnInfo> columns,
            AgentOpsInfo agentOperations) {

        static SchemaInfo from(RecordSchema schema) {
            var columns = schema.getColumns().stream()
                    .map(c -> new ColumnInfo(c.name(), c.type().name(), c.required()))
                    .toList();
            var ops = schema.getAgentOperations();
            var agentOps = ops != null
                    ? new AgentOpsInfo(ops.createEnabled(), ops.updateEnabled(), ops.deleteEnabled())
                    : null;
            return new SchemaInfo(schema.getName(), columns, agentOps);
        }
    }

    record ColumnInfo(String name, String type, boolean required) {}

    record AgentOpsInfo(boolean createEnabled, boolean updateEnabled, boolean deleteEnabled) {}

    record PaginatedRecords(
            List<Record> content,
            int page,
            int size,
            long totalElements,
            int totalPages) {}
}
