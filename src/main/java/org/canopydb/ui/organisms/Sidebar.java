package org.canopydb.ui.organisms;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TreeViewEventController;
import org.canopydb.services.ConnectionMetadataService;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.interfaces.TableDataAppendAction;


public class Sidebar {
    private final TreeViewEventController treeViewEventController = new TreeViewEventController();
    private final TableDataAppendAction tableDataAppendAction;

    public Sidebar(TableDataAppendAction tableDataAppendAction) {
        this.tableDataAppendAction = tableDataAppendAction;
    }

    private TreeView<String> getStringTreeView(TreeItem<String> rootDatabases) {
        TreeView<String> databaseTreeView = new TreeView<>(rootDatabases);
        databaseTreeView.setOnMouseClicked(
                event -> treeViewEventController.nodeClickEventHandler(
                    event, tableDataAppendAction, databaseTreeView
                )
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
