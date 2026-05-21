package org.canopydb.ui.organisms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.entities.TableData;
import org.canopydb.ui.utils.TableComponent;

import java.util.HashMap;
import java.util.HashSet;
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

    private void setSortEventListener(String tableName, String databaseName, TableView<List<String>> tableView){
        tableView.setSortPolicy(tv -> {
            System.out.println("\n SORT POLICY TRIGGERED \n");
            if (tv.getSortOrder().isEmpty()) return true;
            String orderBy = "";
            int orderDirection = 0;
            TableColumn<List<String>, ?> selectedColumn = tv.getSortOrder().getFirst();
            TableColumn.SortType sortType = selectedColumn.getSortType();
            orderBy = selectedColumn.getText();
            orderDirection = sortType == TableColumn.SortType.ASCENDING ? 0 : 1;
            tableViewEventController.tableReRender(
                    tableName,
                    databaseName,
                    orderBy,
                    orderDirection,
                    tv
            );
            return true;
        });

//        tableView.setOnSort(event -> {
//            event.consume();
//
//            ObservableList<TableColumn<List<String>, ?>> sortOrder = tableView.getSortOrder();
//            if (!sortOrder.isEmpty()) {
//                TableColumn<List<String>, ?> column = sortOrder.getFirst();
//
//                if (column.getSortType() == TableColumn.SortType.ASCENDING) {
//                    column.setSortType(TableColumn.SortType.DESCENDING);
//                } else {
//                    column.setSortType(TableColumn.SortType.ASCENDING);
//                }
//
//                String orderBy = column.getText();
//                int orderDirection = (column.getSortType() == TableColumn.SortType.ASCENDING) ? 0 : 1;
//
//                tableViewEventController.tableReRender(
//                    tableName,
//                    databaseName,
//                    orderBy,
//                    orderDirection,
//                    tableView
//                );
//                // 3. Trigger your SQL reload here
//                // List<MyData> newData = database.fetchSorted(columnName, direction);
//                // tableView.getItems().setAll(newData);
//            }
//        });
    }

    public void addTable(TableData tableData) {
        TableView<List<String>> tableView = TableComponent.buildTableComponent(tableData);
        String tablePath = tableData.getTablePath();
        Tab newTab = buildTab(tablePath, tableView);
        tabs.getTabs().add(newTab);
        activeTables.put(tablePath, tableData);

        this.setSortEventListener(
                tableData.getTableName(),
                tableData.getDatabaseName(),
                tableView
        );
    }

    public void updateTable(TableData tableData, TableView<List<String>> tableView) {
        TableComponent.updateTableContents(tableData, tableView);
//        this.setSortEventListener(tableData.getTableName(), tableData.getDatabaseName(), tableView);
    }

    public boolean isTableOpen(String table) {
        return activeTables.containsKey(table);
    }
}
