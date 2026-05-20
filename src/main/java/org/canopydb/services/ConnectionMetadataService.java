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

    public CompletableFuture<List<String>> loadDatabaseAsyncV2(TreeItem<String> node){
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

    public void loadDatabasesAsync(TreeItem<String> node) {
        if (node.getChildren().isEmpty()) return;
        String firstChildValue = node.getChildren().getFirst().getValue();
        if (!firstChildValue.equals("Loading") &&
                !firstChildValue.equals("Error")){
            return;
        }

        Task<List<String>> fetchDatabases = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return metadataDAO.getAllDatabases();
            }
        };

        fetchDatabases.setOnSucceeded(event -> {
            List<String> dbList = fetchDatabases.getValue();
            if (!node.getChildren().isEmpty()) node.getChildren().clear();
            for (String dbName : dbList) {
                TreeItem<String> dbItem = new TreeItem<>(dbName);
                dbItem.getChildren().add(new TreeItem<>("Loading"));
                dbItem.addEventHandler(TreeItem.<String>branchExpandedEvent(), dbEvent -> {
                    loadTablesAsync(dbEvent.getSource());
                });
                node.getChildren().add(dbItem);
            }
        });

        fetchDatabases.setOnFailed(event -> {
            Throwable error = fetchDatabases.getException();
            System.err.println("Failed to fetch databases: " + error.getMessage());
            if (!node.getChildren().isEmpty()) node.getChildren().clear();
            node.getChildren().add(new TreeItem<>("Error"));
        });

        ExecutorService exec = ThreadPool.getExecutor();
        exec.submit(fetchDatabases);
    }

    public void loadTablesAsync(TreeItem<String> node) {
        if (node.getChildren().isEmpty()) return;
        String firstChildValue = node.getChildren().getFirst().getValue();
        if (!firstChildValue.equals("Loading") && !firstChildValue.equals("Error")){
            return;
        }

        Task<List<String>> fetchTables = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                return metadataDAO.getAllTablesByDatabase(node.getValue());
            }
        };

        fetchTables.setOnSucceeded(event -> {
            List<String> tables = fetchTables.getValue();
            if (!node.getChildren().isEmpty()) node.getChildren().clear();
            for (String table: tables){
                TreeItem<String> tableItem = new TreeItem<>(table);
                node.getChildren().add(tableItem);
            }
        });

        fetchTables.setOnFailed(event -> {
            Throwable error = fetchTables.getException();
            System.err.println("Failed to fetch tables: " + error.getMessage());
            if (!node.getChildren().isEmpty()) node.getChildren().clear();
            node.getChildren().add(new TreeItem<>("Error"));
        });

        ExecutorService exec = ThreadPool.getExecutor();
        exec.submit(fetchTables);
    }
}
