package com.nexora.shared.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class QueryDispatcher {

    private static final Logger log = LoggerFactory.getLogger(QueryDispatcher.class);

    private final Map<Class<? extends Query>, QueryHandler<?, ?>> handlers = new HashMap<>();

    public QueryDispatcher(List<QueryHandler<?, ?>> queryHandlers) {
        for (QueryHandler<?, ?> handler : queryHandlers) {
            handlers.put(handler.queryType(), handler);
            log.info("Registered query handler: {} for query: {}",
                handler.getClass().getSimpleName(),
                handler.queryType().getSimpleName());
        }
    }

    @SuppressWarnings("unchecked")
    public <Q extends Query, R> R dispatch(Q query) {
        QueryHandler<Q, R> handler = (QueryHandler<Q, R>) handlers.get(query.getClass());

        if (handler == null) {
            throw new IllegalArgumentException(
                "No handler registered for query: " + query.getClass().getSimpleName());
        }

        log.debug("Dispatching query: {}", query.getClass().getSimpleName());

        return handler.handle(query);
    }
}
