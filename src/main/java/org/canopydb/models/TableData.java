package org.canopydb.models;

import org.canopydb.queries.TableQuery;
import org.canopydb.utils.TableUtilities;

import java.util.ArrayList;
import java.util.List;

public class TableData {
    private final List<String> headers = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();

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
