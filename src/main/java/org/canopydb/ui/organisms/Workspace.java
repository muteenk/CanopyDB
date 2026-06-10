package org.canopydb.ui.organisms;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
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

/*                  Workspace Responsibility
* The single responsibility for Workspace is to manage sessions.
* Neither table internals nor tab internals, just sessions.
*
* A session here refers to an active tab along with its table session component.
* */

public class Workspace {
    private final VBox workspace = new VBox();
    private final TabPane tabs = new TabPane();

    private final HashMap<String, TableSession> activeSessions = new HashMap<>();
    private final HashMap<String, Tab> activeTabs = new HashMap<>();

    private final TableViewEventController tableViewEventController;

    public Workspace(PushNotification pushNotification){
        tableViewEventController = new TableViewEventController(
                this::updateTable,
                pushNotification
        );
        workspace.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
    }

    public VBox getWorkspace() {
        return workspace;
    }

    private void buildPaginator(HBox tableFooter, TableSession tableSession, TableView<List<String>> tableView) {
        Button left = new Button("◀");
        Button right = new Button("▶");

        left.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            boolean isPrev = tableSession.getPreviousPage();
            if (isPrev) tableViewEventController.tableReRender(tableSession, tableView);
        });

        right.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            boolean isNext = tableSession.getNextPage();
            if (isNext) tableViewEventController.tableReRender(tableSession, tableView);
        });

        TablePagination pagination = tableSession.getPaginationData();
        Label paginationLabel = new Label(
                ((pagination.totalRows() > 0) ? pagination.offset()+1 : 0) +
                        " - " +
                Math.min(pagination.offset() + pagination.limit(), pagination.totalRows()) +
                        " of " +
                pagination.totalRows()
        );

        tableFooter.getStyleClass().add("table-footer");

        left.getStyleClass().add("pagination-button");
        right.getStyleClass().add("pagination-button");

        left.setDisable(!tableSession.hasPrevious());
        right.setDisable(!tableSession.hasNext());

        paginationLabel.getStyleClass().add("pagination-label");

        tableFooter.getChildren().addAll(
                left,
                paginationLabel,
                right
        );
    }

    private HBox buildFooter(TableView<List<String>> tableView, TableSession tableSession) {
        HBox tableFooter = new HBox();
        tableFooter.setPrefHeight(40);
        buildPaginator(tableFooter, tableSession, tableView);
        return tableFooter;
    }

    private VBox buildTabContent(TableView<List<String>> tableView, TableSession tableSession) {
        VBox tableBox = new VBox();
        HBox tableFooter = buildFooter(tableView, tableSession);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableBox.getChildren().addAll(tableView, tableFooter);
        return tableBox;
    }

    private Tab buildTab(TableSession tableSession, TableView<List<String>> tableView) {
        Tab tab = new Tab(tableSession.getTablePath());
        tab.setContent(buildTabContent(tableView, tableSession));
        tab.setOnClosed(event -> {
            String tablePath = tableSession.getTablePath();
            activeSessions.remove(tablePath);
            activeTabs.remove(tablePath);
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

    public void addNewSession(TableSession tableSession) {
        TableView<List<String>> tableView = TableComponent.buildTableComponent(tableSession.getTableData());
        Tab newTab = buildTab(tableSession, tableView);
        tabs.getTabs().add(newTab);
        activeSessions.put(tableSession.getTablePath(), tableSession);
        activeTabs.put(tableSession.getTablePath(), newTab);
        this.setSortEventListener(
                tableSession,
                tableView
        );
    }

    public void updateTable(TableSession tableSession, TableView<List<String>> tableView) {
        TableComponent.updateTableContents(tableSession.getTableData(), tableView);
        Tab tab = activeTabs.get(tableSession.getTablePath());
        VBox tableBox = (VBox) tab.getContent();
        HBox tableFooter = buildFooter(tableView, tableSession);
        tableBox.getChildren().removeLast();
        tableBox.getChildren().add(tableFooter);
        activeSessions.put(tableSession.getTablePath(), tableSession);
    }

    public boolean isSessionActive(String table) {
        return activeSessions.containsKey(table);
    }
}
