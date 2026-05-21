package org.canopydb.ui.organisms;

import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TreeViewEventController;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.interfaces.TableActiveCheck;
import org.canopydb.ui.interfaces.TableOpenAction;


public class Sidebar {
    private final TreeViewEventController treeViewEventController;

    public Sidebar(TableOpenAction tableDataAppendAction, TableActiveCheck tableActiveCheck) {
        treeViewEventController = new TreeViewEventController(tableDataAppendAction, tableActiveCheck);
    }

    private TreeView<String> buildTreeView(TreeItem<String> rootDatabases) {
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

    private TreeItem<String> getConnectionRoot() {
        TreeItem<String> rootDatabases = new TreeItem<>("Connection");
        rootDatabases.getChildren().add(new TreeItem<>("Loading"));
        rootDatabases.addEventHandler(
                TreeItem.<String>branchExpandedEvent(),
                event -> {
                    treeViewEventController
                            .dbRootExpandHandler(event.getSource());
                }
        );
        return rootDatabases;
    }

    public VBox getSidebar() {
        TextField searchInput = new TextInput("Search").getTextField();
        TreeView<String> databaseTreeView = buildTreeView(getConnectionRoot());
        VBox sidebar = new VBox(searchInput, databaseTreeView);
        VBox.setVgrow(databaseTreeView, Priority.ALWAYS);

        return sidebar;
    }

}
