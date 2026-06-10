package org.canopydb.ui.organisms;

import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TablePagination;
import org.canopydb.models.TableSession;
import org.canopydb.ui.utils.TableComponent;

import java.util.List;

public class TableTab {
    private final TableView<List<String>> tableView;
    private final Tab tab;

    private final TableViewEventController tableViewEventController;

    public TableTab(TableSession tableSession, TableViewEventController tableViewEventController) {
        this.tableView = TableComponent.buildTableComponent(tableSession.getTableData());
        this.tableViewEventController = tableViewEventController;
        setSortEventListener(tableSession);
        this.tab = buildTab(tableSession);
    }

    public void updateSession(TableSession tableSession) {
        TableComponent.updateTableContents(tableSession.getTableData(), tableView);
        VBox tableBox = (VBox) tab.getContent();
        HBox tableFooter = buildFooter(tableView, tableSession);
        tableBox.getChildren().removeLast();
        tableBox.getChildren().add(tableFooter);
    }

    private Tab buildTab(TableSession tableSession) {
        Tab tab = new Tab(tableSession.getTablePath());
        tab.setContent(buildTabContent(tableView, tableSession));
        return tab;
    }

    public Tab getTab() {return this.tab;}

    private void setSortEventListener(
            TableSession tableSession
    ){
        tableView.setSortPolicy(tv -> {
            if (tv.getSortOrder().isEmpty()) return true;
            TableColumn<List<String>, ?> selectedColumn = tv.getSortOrder().getFirst();
            String orderBy = selectedColumn.getText();
            tableSession.setQueryOrder(orderBy);
            tableViewEventController.tableReRender(tableSession);
            return true;
        });
    }

    private VBox buildTabContent(TableView<List<String>> tableView, TableSession tableSession) {
        VBox tableBox = new VBox();
        HBox tableFooter = buildFooter(tableView, tableSession);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableBox.getChildren().addAll(tableView, tableFooter);
        return tableBox;
    }

    private HBox buildFooter(TableView<List<String>> tableView, TableSession tableSession) {
        HBox tableFooter = new HBox();
        tableFooter.setPrefHeight(40);
        buildPaginator(tableFooter, tableSession, tableView);
        return tableFooter;
    }

    private void buildPaginator(HBox tableFooter, TableSession tableSession, TableView<List<String>> tableView) {
        Button left = new Button("◀");
        Button right = new Button("▶");

        left.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            boolean isPrev = tableSession.getPreviousPage();
            if (isPrev) tableViewEventController.tableReRender(tableSession);
        });

        right.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            boolean isNext = tableSession.getNextPage();
            if (isNext) tableViewEventController.tableReRender(tableSession);
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
}
