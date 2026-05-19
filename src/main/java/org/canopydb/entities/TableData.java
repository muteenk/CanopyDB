package org.canopydb.entities;

import java.util.ArrayList;
import java.util.List;

public class TableData {
    List<String> headers;
    List<List<String>> rows;

    public TableData() {
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
}
