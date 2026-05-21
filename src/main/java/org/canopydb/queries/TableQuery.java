package org.canopydb.queries;

import java.util.ArrayList;
import java.util.List;

public class TableQuery {
    private String databaseName;
    private String tableName;
    private final int limit = 300;
    private int offset = 0;
    private List<String> orderColumns = new ArrayList<>();
    private int orderDirection = 0;
    private String where = "";
    private List<String> columns = new ArrayList<>() {{add("*");}};

    public TableQuery(String databaseName, String tableName) {
        this.databaseName = databaseName;
        this.tableName = tableName;
    }

    private String getStringifiedColumns(List<String> cols) {
        StringBuilder colString = new StringBuilder();
        int currentCol = 0;
        while (currentCol < cols.size()-1) {
            colString.append("\t").append(cols.get(currentCol)).append(",\n");
            currentCol++;
        }
        colString.append("\t").append(cols.get(currentCol)).append("\n");
        return colString.toString();
    }

    public void setOrderColumns(String orderColumn, int orderDirection) {
        this.orderColumns.clear();
        if (orderColumn.isEmpty()) return;
        this.orderColumns = List.of(orderColumn);
        this.orderDirection = orderDirection;
    }

    public String getQuery(){
        StringBuilder sql = new StringBuilder();
        sql
            .append("SELECT\n")
            .append(String.join(", ", this.columns)).append("\n")
            .append("FROM `").append(this.databaseName).append("`.`").append(this.tableName).append("`\n");

        if (!this.where.isEmpty()){
            sql.append("WHERE\n").append(this.where);
        }

        if (!this.orderColumns.isEmpty()){
            sql.append("ORDER BY ").append(String.join(", ", this.orderColumns)).append(" ");
            if (orderDirection == 0) sql.append("ASC\n");
            else sql.append("DESC\n");
        }

        sql.append("LIMIT ").append(this.limit).append(" OFFSET ").append(this.offset);
        sql.append(";");
        return sql.toString();
    }
}
