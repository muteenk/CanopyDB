package org.canopydb.ui.organisms;

import javafx.concurrent.Task;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.services.ConnectionMetadataService;

public class Sidebar {
    private final ConnectionMetadataService cms = new ConnectionMetadataService();

    public VBox getSidebar() {
        TreeItem<String> rootDatabases = new TreeItem<>("Databases");
        rootDatabases.getChildren().add(new TreeItem<>("Loading"));
        rootDatabases.addEventHandler(TreeItem.<String>branchExpandedEvent(), event -> {
            cms.loadDatabasesAsync(event.getSource());
        });

        TextField searchInput = new TextField();
        searchInput.setPromptText("Search");

        TreeView<String> databaseTreeView = new TreeView<>(rootDatabases);
        VBox sidebar = new VBox(searchInput, databaseTreeView);
        VBox.setVgrow(databaseTreeView, Priority.ALWAYS);

        return sidebar;
    }
}
