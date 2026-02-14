package com.nexora.recordschema.query;

import com.nexora.shared.query.Query;

public record ListRecordsQuery(
    String schemaName,
    int page,
    int size,
    String filter
) implements Query {
}
