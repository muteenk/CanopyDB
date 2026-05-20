package org.canopydb.services;

import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import org.canopydb.config.ThreadPool;
import org.canopydb.repository.MetadataDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class ConnectionMetadataService {
    private final MetadataDAO metadataDAO = new MetadataDAO();

    public CompletableFuture<List<String>> loadDatabaseAsync(){
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return metadataDAO.getAllDatabases();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                ThreadPool.getExecutor()
        );
    }

    public CompletableFuture<List<String>> loadDBTablesAsync(String database) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return metadataDAO.getAllTablesByDatabase(database);
                    } catch (SQLException e){
                        throw new RuntimeException(e);
                    }
                },
                ThreadPool.getExecutor()
        );
    }
}
