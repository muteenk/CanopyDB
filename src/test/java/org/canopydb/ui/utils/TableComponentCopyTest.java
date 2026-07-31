package org.canopydb.ui.utils;

import org.canopydb.models.CellValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Lesson: extract (or package-access) pure helpers from UI classes so you can test
 * copy rules without launching JavaFX controls.
 */
class TableComponentCopyTest {

    @Test
    void toCopyText_nullCellBecomesNULL() {
        assertEquals("NULL", TableComponent.toCopyText(null));
        assertEquals("NULL", TableComponent.toCopyText(CellValue.ofNull()));
    }

    @Test
    void toCopyText_emptyBecomesSentinel() {
        assertEquals("__EMPTY__", TableComponent.toCopyText(CellValue.of("")));
    }

    @Test
    void toCopyText_keepsNormalValues() {
        assertEquals("alice", TableComponent.toCopyText(CellValue.of("alice")));
    }

    @Test
    void formatRowForCopy_joinsWithCommas() {
        String csv = TableComponent.formatRowForCopy(List.of(
                CellValue.of("1"),
                CellValue.ofNull(),
                CellValue.of(""),
                CellValue.of("ok")
        ));

        assertEquals("1,NULL,__EMPTY__,ok", csv);
    }
}
