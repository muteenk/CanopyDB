package org.canopydb.services;

import org.canopydb.config.ThreadPool;
import org.canopydb.models.TableSession;
import org.canopydb.repository.QueryHandle;
import org.canopydb.repository.TableActionDAO;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class TableActionService {
    private final TableActionDAO tableActionDAO = new TableActionDAO();

    public AsyncQuery<TableSession> loadTableDataAsync(String table, String database) {
        QueryHandle handle = new QueryHandle();
        CompletableFuture<TableSession> future = CompletableFuture.supplyAsync(() -> {
            try {
                TableSession session = new TableSession(table, database);
                loadSession(session, handle);
                return session;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, ThreadPool.getExecutor());
        return new AsyncQuery<>(future, handle);
    }

    public AsyncQuery<TableSession> loadTableDataAsync(TableSession session) {
        QueryHandle handle = new QueryHandle();
        CompletableFuture<TableSession> future = CompletableFuture.supplyAsync(() -> {
            try {
                loadSession(session, handle);
                return session;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, ThreadPool.getExecutor());
        return new AsyncQuery<>(future, handle);
    }

    private void loadSession(TableSession session, QueryHandle handle) throws SQLException {
        session.setTableData(tableActionDAO.getTableData(session.emitQuery(), handle));
        handle.checkCancelled();
        session.setTotalRowCount(tableActionDAO.getTableDataCount(session.emitCountQuery(), handle));
    }
}
