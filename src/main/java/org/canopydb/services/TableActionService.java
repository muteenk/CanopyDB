package org.canopydb.services;

import org.canopydb.config.ThreadPool;
import org.canopydb.repository.TableActionDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TableActionService {
    TableActionDAO tableActionDAO = new TableActionDAO();

    public CompletableFuture<List<List<String>>> loadTableDataAsyncV2(String table, String database){
        return CompletableFuture.supplyAsync(() -> {
            try {
                return tableActionDAO.getTableData(table, database);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, ThreadPool.getExecutor());
    }
}
