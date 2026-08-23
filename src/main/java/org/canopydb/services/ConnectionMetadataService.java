package org.canopydb.services;

import org.canopydb.config.ThreadPool;
import org.canopydb.models.TableSession;
import org.canopydb.repository.MetadataDAO;
import org.canopydb.repository.QueryHandle;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ConnectionMetadataService {
    private final MetadataDAO metadataDAO = new MetadataDAO();

    public AsyncQuery<List<String>> loadDatabaseAsync() {
        QueryHandle handle = new QueryHandle();
        CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return metadataDAO.getAllDatabases(handle);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                ThreadPool.getExecutor()
        );
        return new AsyncQuery<>(future, handle);
    }

    public AsyncQuery<List<String>> loadDBTablesAsync(String database) {
        QueryHandle handle = new QueryHandle();
        CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return metadataDAO.getAllTablesByDatabase(database, handle);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                ThreadPool.getExecutor()
        );
        return new AsyncQuery<>(future, handle);
    }
}
