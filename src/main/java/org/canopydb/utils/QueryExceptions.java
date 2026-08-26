package org.canopydb.utils;

import org.canopydb.repository.QueryCancelledException;

public final class QueryExceptions {

    private QueryExceptions() {
    }

    public static boolean isCancellation(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof QueryCancelledException
                    || current instanceof java.util.concurrent.CancellationException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("query cancelled")
                        || lower.contains("statement cancelled")
                        || lower.contains("query execution was interrupted")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
