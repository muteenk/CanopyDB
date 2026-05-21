package org.canopydb.services;

import org.canopydb.config.ThreadPool;
import org.canopydb.entities.TableData;
import org.canopydb.queries.TableQuery;
import org.canopydb.repository.TableActionDAO;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class TableActionService {
    TableActionDAO tableActionDAO = new TableActionDAO();

    public CompletableFuture<TableData> loadTableDataAsync(
            String table, String database, String orderBy, int orderDirection){
        return CompletableFuture.supplyAsync(() -> {
            try {
                TableQuery query = new TableQuery(database, table);
                query.setOrderColumns(orderBy, orderDirection);
                return tableActionDAO.getTableData(table, database, query.getQuery());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, ThreadPool.getExecutor());
    }
}
