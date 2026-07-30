package org.canopydb.ui.organisms.workspace;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.config.Profiler;
import org.canopydb.controllers.TreeViewEventController;
import org.canopydb.ui.interfaces.TableActiveCheck;
import org.canopydb.ui.interfaces.TableOpenAction;
import org.canopydb.utils.Constants;

/**
 * Workspace left pane: connection tree shell + search composition.
 * Search/filter behavior lives in {@link ConnectionTreeSearch}.
 */
public class Sidebar {

    private final TreeViewEventController treeViewEventController;
    private final TreeItem<String> connectionRoot = new TreeItem<>("Connection");
    private final ConnectionTreeSearch treeSearch;

    public Sidebar(TableOpenAction tableOpenAction, TableActiveCheck tableActiveCheck) {
        treeViewEventController = new TreeViewEventController(tableOpenAction, tableActiveCheck);
        treeSearch = new ConnectionTreeSearch(connectionRoot, treeViewEventController);
        treeViewEventController.setOnTreeDataChanged(treeSearch::onTreeDataChanged);
    }

    public VBox getSidebar() {
        TreeView<String> databaseTreeView = buildTreeView();

        VBox sidebar = new VBox(
                8,
                treeSearch.getSearchField(),
                treeSearch.getEmptySearchLabel(),
                databaseTreeView
        );
        sidebar.getStyleClass().add("sidebar");
        sidebar.setFillWidth(true);
        VBox.setVgrow(databaseTreeView, Priority.ALWAYS);

        return sidebar;
    }

    private TreeView<String> buildTreeView() {
        connectionRoot.getChildren().add(new TreeItem<>(Constants.LOADING));
        connectionRoot.addEventHandler(
                TreeItem.<String>branchExpandedEvent(),
                event -> {
                    if (event.getSource() != connectionRoot) return;
                    treeViewEventController.dbRootExpandHandler(connectionRoot);
                }
        );

        TreeView<String> databaseTreeView = new TreeView<>(connectionRoot);
        databaseTreeView.setShowRoot(true);
        databaseTreeView.setOnMouseClicked(
                event -> {
                    if (event.getClickCount() == 2) {
                        TreeItem<String> selectedItem = databaseTreeView
                                .getSelectionModel().getSelectedItem();
                        treeViewEventController.nodeClickEventHandler(selectedItem);
                        Profiler.logMemory();
                    }
                }
        );
        return databaseTreeView;
    }
}
