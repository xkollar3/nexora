package com.nexora.shared.query;

/**
 * Interface for query handlers.
 * Each query type should have exactly one handler.
 *
 * @param <Q> the query type this handler processes
 * @param <R> the result type returned
 */
public interface QueryHandler<Q extends Query, R> {

    R handle(Q query);

    Class<Q> queryType();
}
