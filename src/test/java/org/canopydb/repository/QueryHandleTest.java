package org.canopydb.repository;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class QueryHandleTest {

    @Test
    void cancel_beforeRegister_preventsExecution() {
        QueryHandle handle = new QueryHandle();
        handle.cancel();

        assertThrows(QueryCancelledException.class, () -> handle.register(mock(Statement.class)));
    }

    @Test
    void cancel_afterRegister_cancelsStatement() throws Exception {
        QueryHandle handle = new QueryHandle();
        Statement statement = mock(Statement.class);

        handle.register(statement);
        handle.cancel();

        verify(statement).cancel();
        assertTrue(handle.isCancelled());
    }

    @Test
    void cancel_beforeStatementRegistered_cancelsWhenRegistered() throws Exception {
        QueryHandle handle = new QueryHandle();
        Statement statement = mock(Statement.class);
        AtomicBoolean cancelCalled = new AtomicBoolean(false);

        handle.cancel();
        assertThrows(QueryCancelledException.class, () -> handle.register(statement));
    }

    @Test
    void checkCancelled_throwsWhenCancelled() {
        QueryHandle handle = new QueryHandle();
        handle.cancel();

        assertThrows(QueryCancelledException.class, handle::checkCancelled);
    }

    @Test
    void clear_allowsReuseUntilCancelledAgain() throws Exception {
        QueryHandle handle = new QueryHandle();
        Statement first = mock(Statement.class);
        Statement second = mock(Statement.class);

        handle.register(first);
        handle.clear();
        handle.register(second);

        assertFalse(handle.isCancelled());
        handle.cancel();
        verify(second).cancel();
    }
}
