package org.canopydb.ui.organisms.workspace;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.config.Profiler;
import org.canopydb.controllers.TreeViewEventController;
import org.canopydb.ui.interfaces.TableActiveCheck;
import org.canopydb.ui.interfaces.TableOpenAction;
import org.canopydb.ui.utils.LazyTreeItem;

/**
 * Workspace left pane: connection tree shell + search composition.
 * Search/filter behavior lives in {@link ConnectionTreeSearch}.
 */
public class Sidebar {

    public static final double MIN_WIDTH = 180;
    public static final double PREF_WIDTH = 260;
    public static final double MAX_WIDTH = 480;

    private final TreeViewEventController treeViewEventController;
    private final LazyTreeItem connectionRoot = new LazyTreeItem("Connection");
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
        sidebar.setMinWidth(MIN_WIDTH);
        sidebar.setPrefWidth(PREF_WIDTH);
        sidebar.setMaxWidth(MAX_WIDTH);
        VBox.setVgrow(databaseTreeView, Priority.ALWAYS);

        return sidebar;
    }

    private TreeView<String> buildTreeView() {
        connectionRoot.addEventHandler(
                TreeItem.<String>branchExpandedEvent(),
                event -> {
                    if (event.getSource() != connectionRoot) {
                        return;
                    }
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
