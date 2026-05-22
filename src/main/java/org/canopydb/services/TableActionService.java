package org.canopydb.services;

import org.canopydb.config.ThreadPool;
import org.canopydb.models.TableData;
import org.canopydb.models.TableSession;
import org.canopydb.queries.Order;
import org.canopydb.queries.TableQuery;
import org.canopydb.repository.TableActionDAO;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class TableActionService {
    TableActionDAO tableActionDAO = new TableActionDAO();

    public CompletableFuture<TableSession> loadTableDataAsync(String table, String database) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TableSession session = new TableSession(table, database);
                session.setTableData(tableActionDAO.getTableData(session.emitQuery()));
                return session;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, ThreadPool.getExecutor());
    }

    public CompletableFuture<TableSession> loadTableDataAsync(TableSession session) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                session.setTableData(tableActionDAO.getTableData(session.emitQuery()));
                return session;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, ThreadPool.getExecutor());
    }
}
