package org.canopydb.models;

import org.canopydb.queries.TableQuery;
import org.canopydb.utils.TableUtilities;

import java.util.ArrayList;
import java.util.List;

public class TableData {
//    private final String tableName;
//    private final String databaseName;
    private final List<String> headers = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();
//    private final TableQuery tableQuery;

//    public TableData(String table, String database, TableQuery query) {
//        tableName = table;
//        databaseName = database;
//        tableQuery = query;
//    }

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

//    public String getTableName() {return this.tableName;}
//
//    public String getDatabaseName() {return this.databaseName;}
//
//    public String getTablePath() {return TableUtilities.tablePath(this.databaseName, this.tableName);}
//
//    public TableQuery getTableQuery() {return this.tableQuery;}
}
