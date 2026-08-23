package org.canopydb.repository;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks an in-flight JDBC statement so another thread can call {@link Statement#cancel()}.
 * Does not impose a query timeout — slow queries are allowed until explicitly cancelled.
 */
public final class QueryHandle {

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicReference<Statement> statement = new AtomicReference<>();

    public void register(Statement statement) throws QueryCancelledException {
        if (cancelled.get()) {
            throw new QueryCancelledException();
        }
        this.statement.set(statement);
        if (cancelled.get()) {
            cancelStatement(statement);
            throw new QueryCancelledException();
        }
    }

    public void clear() {
        statement.set(null);
    }

    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            Statement active = statement.get();
            if (active != null) {
                cancelStatement(active);
            }
        }
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void checkCancelled() throws QueryCancelledException {
        if (cancelled.get()) {
            throw new QueryCancelledException();
        }
    }

    private static void cancelStatement(Statement statement) {
        try {
            statement.cancel();
        } catch (SQLException ignored) {
            // Driver may reject cancel if the statement already finished.
        }
    }
}
