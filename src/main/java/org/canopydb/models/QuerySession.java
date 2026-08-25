package org.canopydb.models;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * State for an ad-hoc SQL editor tab.
 */
public class QuerySession {

    private static final AtomicInteger NEXT_NUMBER = new AtomicInteger(1);

    private final String id;
    private final String title;
    private String sql;

    public QuerySession() {
        this.id = UUID.randomUUID().toString();
        this.title = "Query " + NEXT_NUMBER.getAndIncrement();
        this.sql = "";
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql == null ? "" : sql;
    }

    public void dispose() {
        sql = "";
    }
}
