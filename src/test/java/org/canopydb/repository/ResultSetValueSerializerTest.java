package org.canopydb.repository;

import org.canopydb.models.CellValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Lesson: mocking — we fake ResultSet answers so we can test serialization
 * without a real database.
 *
 * Pattern: when(mock.method()).thenReturn(value);
 */
@ExtendWith(MockitoExtension.class)
class ResultSetValueSerializerTest {

    @Mock
    ResultSet rs;

    @Test
    void stringColumn_readsText() throws SQLException {
        when(rs.getString(1)).thenReturn("hello");
        when(rs.wasNull()).thenReturn(false);

        CellValue cell = ResultSetValueSerializer.read(rs, 1, Types.VARCHAR);

        assertEquals("hello", cell.getText());
        assertTrue(!cell.isNull());
    }

    @Test
    void stringColumn_wasNull_returnsSqlNull() throws SQLException {
        when(rs.getString(1)).thenReturn(null);
        when(rs.wasNull()).thenReturn(true);

        CellValue cell = ResultSetValueSerializer.read(rs, 1, Types.VARCHAR);

        assertTrue(cell.isNull());
        assertEquals("NULL", cell.toDisplayString());
    }

    @Test
    void temporal_usesGetString() throws SQLException {
        when(rs.getString(1)).thenReturn("0000-00-00");
        when(rs.wasNull()).thenReturn(false);

        CellValue cell = ResultSetValueSerializer.read(rs, 1, Types.DATE);

        assertEquals("0000-00-00", cell.getText());
    }

    @Test
    void integer_readsAsPlainString() throws SQLException {
        when(rs.getInt(1)).thenReturn(42);
        when(rs.wasNull()).thenReturn(false);

        CellValue cell = ResultSetValueSerializer.read(rs, 1, Types.INTEGER);

        assertEquals("42", cell.getText());
    }

    @Test
    void decimal_usesPlainString() throws SQLException {
        when(rs.getBigDecimal(1)).thenReturn(new BigDecimal("19.90"));
        when(rs.wasNull()).thenReturn(false);

        CellValue cell = ResultSetValueSerializer.read(rs, 1, Types.DECIMAL);

        assertEquals("19.90", cell.getText());
    }

    @Test
    void boolean_trueBecomesOne() throws SQLException {
        when(rs.getBoolean(1)).thenReturn(true);
        when(rs.wasNull()).thenReturn(false);

        CellValue cell = ResultSetValueSerializer.read(rs, 1, Types.BOOLEAN);

        assertEquals("1", cell.getText());
    }

    @Test
    void binary_summarizesByteLength() throws SQLException {
        when(rs.getBytes(1)).thenReturn(new byte[]{1, 2, 3, 4});
        when(rs.wasNull()).thenReturn(false);

        CellValue cell = ResultSetValueSerializer.read(rs, 1, Types.BLOB);

        assertEquals("<BINARY 4 bytes>", cell.getText());
    }
}
