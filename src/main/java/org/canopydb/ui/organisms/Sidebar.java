package org.canopydb.ui.organisms;

import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TreeViewEventController;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.interfaces.TableDataAppendAction;


public class Sidebar {
    TreeViewEventController treeViewEventController;

    public Sidebar(TableDataAppendAction tableDataAppendAction) {
        treeViewEventController = new TreeViewEventController(tableDataAppendAction);
    }

    private TreeView<String> getStringTreeView(TreeItem<String> rootDatabases) {
        TreeView<String> databaseTreeView = new TreeView<>(rootDatabases);
        databaseTreeView.setOnMouseClicked(
                event -> {
                    if (event.getClickCount() == 2) {
                        TreeItem<String> selectedItem = databaseTreeView.getSelectionModel().getSelectedItem();
                        treeViewEventController.nodeClickEventHandler(
                                selectedItem
                        );
                    }
                }
        );
        return databaseTreeView;
    }

    public VBox getSidebar() {
        TreeItem<String> rootDatabases = new TreeItem<>("Connection");
        rootDatabases.getChildren().add(new TreeItem<>("Loading"));
        rootDatabases.addEventHandler(
            TreeItem.<String>branchExpandedEvent(),
            event -> {
                treeViewEventController
                .dbRootExpandHandler(event.getSource());
            }
        );

        TextField searchInput = new TextInput("Search").getTextField();
        TreeView<String> databaseTreeView = getStringTreeView(rootDatabases);
        VBox sidebar = new VBox(searchInput, databaseTreeView);
        VBox.setVgrow(databaseTreeView, Priority.ALWAYS);

        return sidebar;
    }

}
