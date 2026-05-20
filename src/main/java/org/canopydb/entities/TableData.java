package org.canopydb.entities;

import java.util.ArrayList;
import java.util.List;

public class TableData {
    private final List<String> headers;
    private final List<List<String>> rows;
    private final String tableName;
    private final String databaseName;

    public TableData(String table, String database) {
        tableName = table;
        databaseName = database;
        headers = new ArrayList<>();
        rows = new ArrayList<>();
    }

    public void appendHeader(String col){
        this.headers.add(col);
    }

    public void appendRow(List<String> row){
        this.rows.add(row);
    }

    public List<String> getHeaders() {
        return this.headers;
    }

    public List<List<String>> getRows() {
        return this.rows;
    }

    public String getTablePath() {return this.databaseName + "/" + this.tableName;}
}
