package org.canopydb.ui;

import javafx.concurrent.Task;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.config.DatabasePool;
import org.canopydb.db.MetadataDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sidebar {
    private final MetadataDAO metadataDAO = new MetadataDAO();

    private void loadDatabasesAsync(TreeItem<String> node) {
        if (!node.getChildren().getFirst().getValue().equals("Fetching...")){
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
            node.getChildren().removeLast();
            for (String dbName : dbList) {
                node.getChildren().add(new TreeItem<>(dbName));
            }
        });

        fetchDatabases.setOnFailed(event -> {
            Throwable error = fetchDatabases.getException();
            System.err.println("Failed to fetch databases: " + error.getMessage());
            node.getChildren().removeLast();
            node.getChildren().add(new TreeItem<>("Error loading databases !"));
        });

        Thread task = new Thread(fetchDatabases);
        task.setDaemon(true);
        task.start();
    }

    public VBox getSidebar() {
//        Map<String, List<String>> databases = loadDatabases();
        TreeItem<String> rootDatabases = new TreeItem<>("Databases");
        rootDatabases.getChildren().add(new TreeItem<>("Fetching..."));
        rootDatabases.addEventHandler(TreeItem.<String>branchExpandedEvent(), event -> {
            loadDatabasesAsync(event.getSource());
        });

//        databases.forEach((database, tables) -> {
//            TreeItem<String> dbItem = new TreeItem<>(database);
//            dbItem.getChildren().addAll(
//                    tables.stream()
//                            .map(TreeItem::new)
//                            .toList()
//            );
//            rootDatabases.getChildren().add(dbItem);
//        });

        TextField searchInput = new TextField();
        searchInput.setPromptText("Search");
        searchInput.setStyle("-fx-background-color: #363840;");

        TreeView<String> databaseTreeView = new TreeView<>(rootDatabases);
        databaseTreeView.setStyle("-fx-background-color: #363840;");
        VBox sidebar = new VBox(searchInput, databaseTreeView);
        VBox.setVgrow(sidebar, Priority.ALWAYS);
        sidebar.setStyle("-fx-background-color: #363840;");
        return sidebar;
    }
}
