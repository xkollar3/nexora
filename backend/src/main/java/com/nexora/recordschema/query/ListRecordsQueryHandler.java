package com.nexora.recordschema.query;

import com.nexora.recordschema.shared.Record;
import com.nexora.shared.query.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ListRecordsQueryHandler implements QueryHandler<ListRecordsQuery, Page<Record>> {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Record> handle(ListRecordsQuery query) {
        var pageable = PageRequest.of(query.page(), query.size());
        var collectionName = query.schemaName();

        var mongoQuery = buildQuery(query);
        mongoQuery.with(pageable);

        var countQuery = buildQuery(query);

        var records = mongoTemplate.find(mongoQuery, Record.class, collectionName);
        var count = mongoTemplate.count(countQuery, Record.class, collectionName);

        return PageableExecutionUtils.getPage(records, pageable, () -> count);
    }

    @Override
    public Class<ListRecordsQuery> queryType() {
        return ListRecordsQuery.class;
    }

    private Query buildQuery(ListRecordsQuery query) {
        var userFilter = query.filter();
        if (userFilter == null || userFilter.isBlank() || "{}".equals(userFilter.trim())) {
            return new Query();
        }
        return new BasicQuery(userFilter);
    }
}
