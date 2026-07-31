package org.canopydb.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableUtilitiesTest {

    @Test
    void tablePath_joinsDatabaseAndTableWithSeparator() {
        // Tab identity in Workspace depends on this exact format.
        assertEquals("app : users", TableUtilities.tablePath("app", "users"));
    }
}
