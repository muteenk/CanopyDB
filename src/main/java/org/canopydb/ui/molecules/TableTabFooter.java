package org.canopydb.ui.molecules;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TablePagination;
import org.canopydb.models.TableSession;

public class TableTabFooter {
    private final HBox tableFooter = new HBox();
    private final TableViewEventController tableViewEventController;

    public TableTabFooter(TableSession tableSession, TableViewEventController tableViewEventController) {
        this.tableViewEventController = tableViewEventController;
        tableFooter.setPrefHeight(40);
        setPaginator(tableSession);
    }

    public HBox getTableFooter() {return this.tableFooter;}

    public void updatePagination(TableSession tableSession) {
        tableFooter.getChildren().clear();
        setPaginator(tableSession);
    }

    private void setPaginator(TableSession tableSession) {
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
