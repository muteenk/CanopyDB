package org.canopydb.ui.organisms;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TableData;
import org.canopydb.queries.Order;
import org.canopydb.ui.utils.TableComponent;
import org.canopydb.utils.TableUtilities;

import java.util.HashMap;
import java.util.List;

public class Workspace {
    private final VBox workspace;
    private final TabPane tabs;
    private final HashMap<String, TableData> activeTables;
    private final TableViewEventController tableViewEventController;

    public Workspace(){
        tableViewEventController = new TableViewEventController(this::updateTable);
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
            String tableName,
            String databaseName,
            TableView<List<String>> tableView
    ){
        tableView.setSortPolicy(tv -> {
            if (tv.getSortOrder().isEmpty()) return true;
            String orderBy = "";
            Order.OrderDirection orderDirection = Order.OrderDirection.ASC;
            TableColumn<List<String>, ?> selectedColumn = tv.getSortOrder().getFirst();
            orderBy = selectedColumn.getText();

            Order order = activeTables
                    .get(TableUtilities.tablePath(databaseName, tableName))
                    .getTableQuery().getOrder();
            if (order.getColumn().equals(orderBy)) {
                orderDirection = order.getDirection() == Order.OrderDirection.ASC ? Order.OrderDirection.DESC : Order.OrderDirection.ASC;
            }
            tableViewEventController.tableReRender(
                    tableName,
                    databaseName,
                    orderBy,
                    orderDirection,
                    tv
            );
            return true;
        });
    }

    public void addTable(TableData tableData) {
        TableView<List<String>> tableView = TableComponent.buildTableComponent(tableData);
        String tablePath = tableData.getTablePath();
        tabs.getTabs().add(buildTab(tablePath, tableView));
        activeTables.put(tablePath, tableData);

        this.setSortEventListener(
                tableData.getTableName(),
                tableData.getDatabaseName(),
                tableView
        );
    }

    public void updateTable(TableData tableData, TableView<List<String>> tableView) {
        TableComponent.updateTableContents(tableData, tableView);
        activeTables.put(tableData.getTablePath(), tableData);
    }

    public boolean isTableOpen(String table) {
        return activeTables.containsKey(table);
    }
}
