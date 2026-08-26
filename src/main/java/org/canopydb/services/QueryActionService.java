package org.canopydb.services;

import org.canopydb.config.ThreadPool;
import org.canopydb.models.QueryResult;
import org.canopydb.repository.QueryActionDAO;
import org.canopydb.repository.QueryHandle;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class QueryActionService {
    private final QueryActionDAO queryActionDAO = new QueryActionDAO();

    public AsyncQuery<QueryResult> executeAsync(String sql) {
        QueryHandle handle = new QueryHandle();
        CompletableFuture<QueryResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                return queryActionDAO.execute(sql, handle);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, ThreadPool.getExecutor());
        return new AsyncQuery<>(future, handle);
    }
}
