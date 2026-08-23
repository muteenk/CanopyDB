package org.canopydb.utils;

import org.canopydb.repository.QueryCancelledException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryExceptionsTest {

    @Test
    void isCancellation_detectsDirectException() {
        assertTrue(QueryExceptions.isCancellation(new QueryCancelledException()));
    }

    @Test
    void isCancellation_detectsWrappedException() {
        Throwable wrapped = new CompletionException(new RuntimeException(new QueryCancelledException()));
        assertTrue(QueryExceptions.isCancellation(wrapped));
    }

    @Test
    void isCancellation_returnsFalseForOtherErrors() {
        assertFalse(QueryExceptions.isCancellation(new RuntimeException("timeout")));
    }
}
