package org.canopydb.models;

import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableDataTest {

    @Test
    void getHeaders_returnsColumnNamesInOrder() {
        TableData data = new TableData();
        data.appendColumn(new ColumnMeta("id", Types.INTEGER, "INT"));
        data.appendColumn(new ColumnMeta("email", Types.VARCHAR, "VARCHAR"));
        data.appendRow(List.of(CellValue.of("1"), CellValue.of("a@b.com")));

        assertEquals(List.of("id", "email"), data.getHeaders());
        assertEquals(1, data.getRows().size());
    }
}
