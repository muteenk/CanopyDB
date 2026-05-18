package org.canopydb.ui;

import javafx.concurrent.Task;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.db.MetadataDAO;

import java.sql.SQLException;
import java.util.List;

public class Sidebar {
    private final MetadataDAO metadataDAO = new MetadataDAO();

    private void loadDatabasesAsync(TreeItem<String> node) {
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

    private void loadTablesAsync(TreeItem<String> node) {
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
                node.getChildren().add(new TreeItem<>(table));
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

    public VBox getSidebar() {
        TreeItem<String> rootDatabases = new TreeItem<>("Databases");
        rootDatabases.getChildren().add(new TreeItem<>("Loading"));
        rootDatabases.addEventHandler(TreeItem.<String>branchExpandedEvent(), event -> {
            loadDatabasesAsync(event.getSource());
        });

        TextField searchInput = new TextField();
        searchInput.setPromptText("Search");
        searchInput.setStyle("-fx-background-color: #363840;");

        TreeView<String> databaseTreeView = new TreeView<>(rootDatabases);
        databaseTreeView.setStyle("-fx-background-color: #363840;");
        VBox sidebar = new VBox(searchInput, databaseTreeView);
        VBox.setVgrow(databaseTreeView, Priority.ALWAYS);
        sidebar.setStyle("-fx-background-color: #363840;");

        return sidebar;
    }
}
