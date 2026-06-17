package org.canopydb.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableQuery {
    private final String databaseName;
    private final String tableName;
    private final List<String> columns = new ArrayList<>();
    private final int limit = 300;
    private int offset = 0;
    private final Order order = new Order();
    private final Map<String, String> where = new HashMap<>();

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
    public void setOffset(int offset) {this.offset=offset;}
    public void resetOffset() {setOffset(0);}

    private String formatFilterText(String filterQuery) {
        return "( " + filterQuery + " )";
    }
    public void addFilter(String key, String filterQuery) {
        where.put(key, formatFilterText(filterQuery));
        resetOffset();
    }
    public void removeFilter(String key) {
        where.remove(key);
        resetOffset();
    }
    public void clearFilter() {
        where.clear();
        resetOffset();
    }

    public String getQuery(){
        StringBuilder sql = new StringBuilder();
        sql
            .append("SELECT\n")
            .append(String.join(", ", this.columns)).append("\n")
            .append("FROM `").append(this.databaseName).append("`.`").append(this.tableName).append("`\n");

        if (!this.where.isEmpty()){
            List<String> filters = new ArrayList<>();
            for (Map.Entry<String, String> entry : where.entrySet()) filters.add(entry.getValue());
            sql.append("WHERE\n\t").append(String.join(" AND\n\t", filters)).append("\n");
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
            List<String> filters = new ArrayList<>();
            for (Map.Entry<String, String> entry : where.entrySet()) filters.add(entry.getValue());
            sql.append("WHERE\n\t").append(String.join(" AND\n\t", filters)).append("\n");
        }

        sql.append(";");
        return sql.toString();
    }
}
