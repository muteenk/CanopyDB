package org.canopydb.services;

import org.canopydb.config.ThreadPool;
import org.canopydb.models.TableData;
import org.canopydb.queries.Order;
import org.canopydb.queries.TableQuery;
import org.canopydb.repository.TableActionDAO;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class TableActionService {
    TableActionDAO tableActionDAO = new TableActionDAO();

    private TableQuery tableRetrivalQueryBuilder(
            String database,
            String table,
            String orderBy,
            Order.OrderDirection orderDirection
    ) {
        TableQuery query = new TableQuery(database, table);
        query.setOrderColumn(orderBy, orderDirection);
        return query;
    }

    public CompletableFuture<TableData> loadTableDataAsync(String table, String database) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return tableActionDAO.getTableData(table, database, tableRetrivalQueryBuilder(database, table, "", Order.OrderDirection.ASC));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, ThreadPool.getExecutor());
    }

    public CompletableFuture<TableData> loadTableDataAsync(
            String table, String database, String orderBy, Order.OrderDirection orderDirection){
        return CompletableFuture.supplyAsync(() -> {
            try {
                return tableActionDAO.getTableData(table, database, tableRetrivalQueryBuilder(database, table, orderBy, orderDirection));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, ThreadPool.getExecutor());
    }
}
