package org.canopydb.ui.organisms;

import javafx.concurrent.Task;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.services.ConnectionMetadataService;
import org.canopydb.services.TableActionService;

public class Sidebar {
    private final ConnectionMetadataService connectionMetadataService = new ConnectionMetadataService();
    private final TableActionService tableActionService = new TableActionService();
    private final MiddlePane middle;

    public Sidebar(MiddlePane middleComponent) {
        this.middle = middleComponent;
    }

    private int getNodeDepth(TreeItem<String> item){
        // Level 0 = Root Server ("Local MySQL Server")
        // Level 1 = Database Node ("my_database")
        // Level 2 = Table Node ("users") -> This is what we want!

        int depth = 0;
        TreeItem<String> current = item;

        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
        }

        return depth;
    }

    private boolean isTableNode(TreeItem<String> item) {
        return getNodeDepth(item) == 2;
    }

    private TreeView<String> getStringTreeView(TreeItem<String> rootDatabases) {
        TreeView<String> databaseTreeView = new TreeView<>(rootDatabases);
        databaseTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<String> selectedItem = databaseTreeView.getSelectionModel().getSelectedItem();

                if (selectedItem != null && isTableNode(selectedItem)) {
                    tableActionService.loadTableDataAsync(selectedItem, middle);
                }
            }
        });
        return databaseTreeView;
    }

    public VBox getSidebar() {
        TreeItem<String> rootDatabases = new TreeItem<>("Databases");
        rootDatabases.getChildren().add(new TreeItem<>("Loading"));
        rootDatabases.addEventHandler(TreeItem.<String>branchExpandedEvent(), event -> {
            connectionMetadataService.loadDatabasesAsync(event.getSource());
        });

        TextField searchInput = new TextField();
        searchInput.setPromptText("Search");

        TreeView<String> databaseTreeView = getStringTreeView(rootDatabases);
        VBox sidebar = new VBox(searchInput, databaseTreeView);
        VBox.setVgrow(databaseTreeView, Priority.ALWAYS);

        return sidebar;
    }

}
