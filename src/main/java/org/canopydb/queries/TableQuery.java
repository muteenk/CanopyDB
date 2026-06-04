package org.canopydb.queries;

import java.util.ArrayList;
import java.util.List;

public class TableQuery {
    private final String databaseName;
    private final String tableName;
    private final List<String> columns = new ArrayList<>();
    private final int limit = 300;
    private int offset = 0;
    private final Order order = new Order();
    private String where = "";

    public TableQuery(String databaseName, String tableName) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        columns.add("*");
    }

    public void setOrderColumn(String orderColumn, Order.OrderDirection orderDirection) {
        this.order.setOrder(orderColumn, orderDirection);
    }

    public Order getOrder() {return this.order;}

    public int getLimit() {return this.limit;}

    public int getOffset() {return this.offset;}

    public String getQuery(){
        StringBuilder sql = new StringBuilder();
        sql
            .append("SELECT\n")
            .append(String.join(", ", this.columns)).append("\n")
            .append("FROM `").append(this.databaseName).append("`.`").append(this.tableName).append("`\n");

        if (!this.where.isEmpty()){
            sql.append("WHERE\n").append(this.where);
        }

        if (!this.order.getColumn().isEmpty()){
            sql.append("ORDER BY ").append(String.join(", ", this.order.getColumn())).append(" ");
            if (this.order.getDirection() == Order.OrderDirection.ASC) sql.append("ASC\n");
            else sql.append("DESC\n");
        }

        sql.append("LIMIT ").append(this.limit).append(" OFFSET ").append(this.offset);
        sql.append(";");
        return sql.toString();
    }

    public String getRowCountQuery() {
        StringBuilder sql = new StringBuilder();
        sql.append(
                "SELECT\n\tCOUNT(*) AS row_count\nFROM `"
        ).append(this.databaseName).append("`.`").append(this.tableName).append("`\n");

        if (!this.where.isEmpty()){
            sql.append("WHERE\n").append(this.where);
        }

        if (!this.order.getColumn().isEmpty()){
            sql.append("ORDER BY ").append(String.join(", ", this.order.getColumn())).append(" ");
            if (this.order.getDirection() == Order.OrderDirection.ASC) sql.append("ASC\n");
            else sql.append("DESC\n");
        }

        sql.append("LIMIT ").append(this.limit).append(" OFFSET ").append(this.offset);
        sql.append(";");
        return sql.toString();
    }
}
