package org.canopydb.ui;

import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sidebar {
    public VBox getSidebar() {
        Map<String, List<String>> databases = new HashMap<>();
        databases.put("Database1", new ArrayList<>(List.of("D1T1", "D1T2", "D1T3")));
        databases.put("Database2", new ArrayList<>(List.of("D2T1", "D2T2")));
        databases.put("Database3", new ArrayList<>(List.of("D3T0", "D3T1", "D3T2")));

        TreeItem<String> rootDatabases = new TreeItem<>("Databases");
        databases.forEach((database, tables) -> {
            TreeItem<String> dbItem = new TreeItem<>(database);
            dbItem.getChildren().addAll(
                    tables.stream()
                            .map(TreeItem::new)
                            .toList()
            );

            // Attach this database node to the main root
            rootDatabases.getChildren().add(dbItem);
        });

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
