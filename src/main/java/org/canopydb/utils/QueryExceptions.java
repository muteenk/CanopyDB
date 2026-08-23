package org.canopydb.utils;

import org.canopydb.repository.QueryCancelledException;

public final class QueryExceptions {

    private QueryExceptions() {
    }

    public static boolean isCancellation(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof QueryCancelledException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
