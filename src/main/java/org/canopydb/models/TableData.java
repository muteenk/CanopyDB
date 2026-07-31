package org.canopydb.models;

import java.util.ArrayList;
import java.util.List;

public class TableData {
    private final List<ColumnMeta> columns = new ArrayList<>();
    private final List<List<CellValue>> rows = new ArrayList<>();

    public void appendColumn(ColumnMeta column) {
        this.columns.add(column);
    }

    public void appendRow(List<CellValue> row) {
        this.rows.add(row);
    }

    public List<ColumnMeta> getColumns() {
        return this.columns;
    }

    public List<String> getHeaders() {
        List<String> headers = new ArrayList<>(columns.size());
        for (ColumnMeta column : columns) {
            headers.add(column.getName());
        }
        return headers;
    }

    public List<List<CellValue>> getRows() {
        return this.rows;
    }

    /** Drop row/column data so a closed tab can be GC'd sooner. */
    public void clear() {
        rows.clear();
        columns.clear();
    }
}
