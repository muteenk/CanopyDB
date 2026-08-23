package org.canopydb.repository;

import java.sql.SQLException;

/** Thrown when a query is aborted via {@link QueryHandle#cancel()}. */
public final class QueryCancelledException extends SQLException {

    public QueryCancelledException() {
        super("Query cancelled");
    }
}
