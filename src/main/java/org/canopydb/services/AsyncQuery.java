package org.canopydb.services;

import org.canopydb.repository.QueryHandle;

import java.util.concurrent.CompletableFuture;

/**
 * An async operation paired with its {@link QueryHandle} so callers can cancel JDBC work.
 */
public record AsyncQuery<T>(CompletableFuture<T> future, QueryHandle handle) {

    public void cancel() {
        handle.cancel();
        future.cancel(true);
    }
}
