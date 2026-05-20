package org.canopydb.ui.organisms;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.services.ConnectionMetadataService;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.interfaces.TableDataAppendAction;

import java.util.List;


public class Sidebar {
    private final ConnectionMetadataService connectionMetadataService = new ConnectionMetadataService();
    private final TableActionService tableActionService = new TableActionService();
    private final TableDataAppendAction tableDataAppendAction;

    public Sidebar(TableDataAppendAction tableDataAppendAction) {
        this.tableDataAppendAction = tableDataAppendAction;
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
                    tableActionService.loadTableDataAsync(
                            selectedItem.getValue(),
                            selectedItem.getParent().getValue()
                    ).thenAccept(data -> {
                        Platform.runLater(() -> {tableDataAppendAction.render(data);});
                    });
                }
            }
        });
        return databaseTreeView;
    }

    private void dbExpandHandler(TreeItem<String> node) {
        if (node.getChildren().isEmpty()) return;
        String firstChildValue = node.getChildren().getFirst().getValue();
        if (!firstChildValue.equals("Loading") && !firstChildValue.equals("Error")){
            return;
        }

        connectionMetadataService
        .loadDBTablesAsync(node.getValue())
        .thenAccept(tables -> {
            Platform.runLater(() -> {
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                for (String table: tables){
                    TreeItem<String> tableItem = new TreeItem<>(table);
                    node.getChildren().add(tableItem);
                }
            });
        })
        .exceptionally(error -> {
            System.err.println("Failed to fetch tables: " + error.getMessage());
            if (!node.getChildren().isEmpty()) node.getChildren().clear();
            node.getChildren().add(new TreeItem<>("Error"));
            return null;
        });
    }

    private void dbRootExpandHandler(TreeItem<String> node) {
        if (node.getChildren().isEmpty()) return;
        String firstChildValue = node.getChildren().getFirst().getValue();
        if (!firstChildValue.equals("Loading") &&
                !firstChildValue.equals("Error")){
            return;
        }

        connectionMetadataService
        .loadDatabaseAsync()
        .thenAccept(dbList -> {
            Platform.runLater(() -> {
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                for (String dbName : dbList) {
                    TreeItem<String> dbItem = new TreeItem<>(dbName);
                    dbItem.getChildren().add(new TreeItem<>("Loading"));
                    dbItem.addEventHandler(TreeItem.<String>branchExpandedEvent(), dbEvent -> {
                        dbExpandHandler(dbEvent.getSource());
                    });
                    node.getChildren().add(dbItem);
                }
            });
        })
        .exceptionally(error -> {
            Platform.runLater(() -> {
                System.err.println("Failed to fetch databases: " + error.getMessage());
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                node.getChildren().add(new TreeItem<>("Error"));
            });
            return null;
        });
    }

    public VBox getSidebar() {
        TreeItem<String> rootDatabases = new TreeItem<>("Databases");
        rootDatabases.getChildren().add(new TreeItem<>("Loading"));
        rootDatabases.addEventHandler(
            TreeItem.<String>branchExpandedEvent(),
            event -> {dbRootExpandHandler(event.getSource());}
        );

        TextField searchInput = new TextInput("Search").getTextField();
        TreeView<String> databaseTreeView = getStringTreeView(rootDatabases);
        VBox sidebar = new VBox(searchInput, databaseTreeView);
        VBox.setVgrow(databaseTreeView, Priority.ALWAYS);

        return sidebar;
    }

}
