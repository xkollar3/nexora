package com.nexora.recordschema.query;

import com.nexora.recordschema.shared.RecordSchema;
import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.shared.query.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class ListSchemasQueryHandler implements QueryHandler<ListSchemasQuery, List<RecordSchema>> {

    private final RecordSchemaRepository schemaRepository;

    @Override
    public List<RecordSchema> handle(ListSchemasQuery query) {
        return schemaRepository.findAll();
    }

    @Override
    public Class<ListSchemasQuery> queryType() {
        return ListSchemasQuery.class;
    }
}
