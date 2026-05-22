package org.canopydb.models;

import org.canopydb.queries.Order;
import org.canopydb.queries.TableQuery;
import org.canopydb.utils.TableUtilities;

public class TableSession {
    private final String tableName;
    private final String databaseName;
    private TableData tableData;
    private final TableQuery tableQuery;

    public TableSession(
            String tableName, String databaseName
    ) {
        this.tableName = tableName;
        this.databaseName = databaseName;
        this.tableData = new TableData();
        this.tableQuery = new TableQuery(databaseName, tableName);
    }

    public String getTablePath() {return TableUtilities.tablePath(this.databaseName, this.tableName);}

    public void setTableData(TableData data) {this.tableData=data;}

    public TableData getTableData() {return this.tableData;}

    public TableQuery getTableQuery() {return this.tableQuery;}


    public void setQueryOrder(
            String orderBy
    ) {
        Order.OrderDirection orderDirection = Order.OrderDirection.ASC;
        Order order = this.getTableQuery().getOrder();
        if (order.getColumn().equals(orderBy)) {
            orderDirection = order.getDirection() == Order.OrderDirection.ASC ? Order.OrderDirection.DESC : Order.OrderDirection.ASC;
        }
        tableQuery.setOrderColumn(orderBy, orderDirection);
    }

    public String emitQuery() {return tableQuery.getQuery();}

}
