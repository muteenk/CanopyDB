package org.canopydb.utils;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Lesson: test the cases that bite users — nested causes, blank messages, wrappers.
 */
class ExceptionMessagesTest {

    @Test
    void nullThrowable_returnsUnknownError() {
        assertEquals("Unknown error", ExceptionMessages.userMessage(null));
    }

    @Test
    void simpleException_usesItsMessage() {
        assertEquals(
                "Access denied",
                ExceptionMessages.userMessage(new RuntimeException("Access denied"))
        );
    }

    @Test
    void nestedException_usesRootCauseMessage() {
        // Arrange — mimics CompletableFuture wrapping a SQLException
        SQLException sql = new SQLException("Table 'app.users' doesn't exist");
        Throwable wrapped = new CompletionException(sql);

        // Act + Assert
        assertEquals(
                "Table 'app.users' doesn't exist",
                ExceptionMessages.userMessage(wrapped)
        );
    }

    @Test
    void blankMessage_fallsBackToClassName() {
        assertEquals(
                "RuntimeException",
                ExceptionMessages.userMessage(new RuntimeException("   "))
        );
    }

    @Test
    void stripsLeadingExceptionClassPrefixFromMessage() {
        assertEquals(
                "Duplicate entry",
                ExceptionMessages.userMessage(
                        new RuntimeException("com.mysql.cj.jdbc.exceptions.MysqlDataTruncationException: Duplicate entry")
                )
        );
    }
}
