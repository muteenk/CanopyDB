package org.canopydb.services;

import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import org.canopydb.repository.MetadataDAO;

import java.awt.event.MouseEvent;
import java.util.List;

public class ConnectionMetadataService {
    private final MetadataDAO metadataDAO = new MetadataDAO();

    public void loadDatabasesAsync(TreeItem<String> node) {
        if (node.getChildren().isEmpty()) return;
        String firstChildValue = node.getChildren().getFirst().getValue();
        if (!firstChildValue.equals("Loading") &&
                !firstChildValue.equals("Error")){
            return;
        }

        Task<List<String>> fetchDatabases = new Task<List<String>>() {
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

        Thread task = new Thread(fetchDatabases);
        task.setDaemon(true);
        task.start();
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

        Thread task = new Thread(fetchTables);
        task.start();
    }
}
