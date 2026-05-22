package org.canopydb.ui.organisms;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TableData;
import org.canopydb.models.TableSession;
import org.canopydb.queries.Order;
import org.canopydb.ui.interfaces.PushNotification;
import org.canopydb.ui.utils.TableComponent;
import org.canopydb.utils.TableUtilities;

import java.util.HashMap;
import java.util.List;

public class Workspace {
    private final VBox workspace;
    private final TabPane tabs;
    private final HashMap<String, TableSession> activeTables;
    private final TableViewEventController tableViewEventController;

    public Workspace(PushNotification pushNotification){
        tableViewEventController = new TableViewEventController(
                this::updateTable,
                pushNotification
        );
        activeTables = new HashMap<>();
        tabs = new TabPane();
        workspace = new VBox(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
    }

    public VBox getWorkspace() {
        return workspace;
    }

    private Tab buildTab(String tablePath, TableView<List<String>> tableView) {
        Tab tab = new Tab(tablePath);
        tab.setContent(tableView);
        tab.setOnClosed(event -> {
            activeTables.remove(tablePath);
        });
        return tab;
    }

    private void setSortEventListener(
            TableSession tableSession,
            TableView<List<String>> tableView
    ){
        tableView.setSortPolicy(tv -> {
            if (tv.getSortOrder().isEmpty()) return true;
            TableColumn<List<String>, ?> selectedColumn = tv.getSortOrder().getFirst();
            String orderBy = selectedColumn.getText();
            tableSession.setQueryOrder(orderBy);
            tableViewEventController.tableReRender(
                    tableSession,
                    tv
            );
            return true;
        });
    }

    public void addTable(TableSession tableSession) {
        TableView<List<String>> tableView = TableComponent.buildTableComponent(tableSession.getTableData());
        String tablePath = tableSession.getTablePath();
        tabs.getTabs().add(buildTab(tablePath, tableView));
        activeTables.put(tablePath, tableSession);

        this.setSortEventListener(
                tableSession,
                tableView
        );
    }

    public void updateTable(TableSession tableSession, TableView<List<String>> tableView) {
        TableComponent.updateTableContents(tableSession.getTableData(), tableView);
        activeTables.put(tableSession.getTablePath(), tableSession);
    }

    public boolean isTableOpen(String table) {
        return activeTables.containsKey(table);
    }
}
