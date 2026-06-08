package org.canopydb.ui.organisms;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TableData;
import org.canopydb.models.TablePagination;
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

    private void buildPaginator(HBox tableFooter, TablePagination pagination) {
        Button left = new Button("◀");
        Button right = new Button("▶");

        Label paginationLabel = new Label(
                pagination.limit() + " - " + pagination.offset() + " of " + pagination.totalRows()
        );

        tableFooter.getStyleClass().add("table-footer");

        left.getStyleClass().add("pagination-button");
        right.getStyleClass().add("pagination-button");

        paginationLabel.getStyleClass().add("pagination-label");

        tableFooter.getChildren().addAll(
                left,
                paginationLabel,
                right
        );
    }

    private VBox buildTabContent(TableView<List<String>> tableView, TablePagination pagination) {
        VBox tableBox = new VBox();
        HBox tableFooter = new HBox();
        tableFooter.setPrefHeight(40);
        buildPaginator(tableFooter, pagination);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableBox.getChildren().addAll(tableView, tableFooter);
        return tableBox;
    }

    private Tab buildTab(TableSession tableSession, TableView<List<String>> tableView) {
        Tab tab = new Tab(tableSession.getTablePath());
        tab.setContent(buildTabContent(tableView, tableSession.getPaginationData()));
        tab.setOnClosed(event -> {
            activeTables.remove(tableSession.getTablePath());
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
        tabs.getTabs().add(buildTab(tableSession, tableView));
        activeTables.put(tableSession.getTablePath(), tableSession);

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
