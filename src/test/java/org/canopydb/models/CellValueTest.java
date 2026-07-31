package org.canopydb.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lesson: start with small models that encode important rules.
 * Rule here: SQL NULL must stay distinct from empty text.
 */
class CellValueTest {

    @Test
    void ofNull_isNullAndDisplaysAsNULL() {
        // Arrange + Act
        CellValue cell = CellValue.ofNull();

        // Assert — check every observable part of the rule
        assertTrue(cell.isNull());
        assertEquals("", cell.getText());
        assertEquals("NULL", cell.toDisplayString());
    }

    @Test
    void of_keepsTextAndIsNotNull() {
        CellValue cell = CellValue.of("hello");

        assertFalse(cell.isNull());
        assertEquals("hello", cell.getText());
        assertEquals("hello", cell.toDisplayString());
    }

    @Test
    void of_treatsJavaNullAsEmptyNonNullCell() {
        // Edge case: Java null in ≠ SQL NULL. Protect this so UI/edit logic stays sane.
        CellValue cell = CellValue.of(null);

        assertFalse(cell.isNull());
        assertEquals("", cell.getText());
        assertEquals("", cell.toDisplayString());
    }

    @Test
    void emptyString_isNotSqlNull() {
        CellValue cell = CellValue.of("");

        assertFalse(cell.isNull());
        assertEquals("", cell.toDisplayString());
    }
}
