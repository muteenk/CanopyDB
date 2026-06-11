package org.canopydb.ui.molecules;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TableSession;
import org.canopydb.ui.atoms.PaginationControls;

public class TableTabFooter {
    private final HBox tableFooter = new HBox();
    private final PaginationControls paginator;

    public TableTabFooter(TableSession tableSession, TableViewEventController tableViewEventController) {
        tableFooter.setPrefHeight(40);
        tableFooter.getStyleClass().add("table-footer");

        this.paginator = new PaginationControls(tableSession.getPaginationData());
        this.paginator.getLeft().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            boolean isPrev = tableSession.setPreviousOffset();
            if (isPrev) tableViewEventController.tableReRender(tableSession);
        });
        this.paginator.getRight().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            boolean isNext = tableSession.setNextOffset();
            if (isNext) tableViewEventController.tableReRender(tableSession);
        });

        tableFooter.getChildren().addAll(
            this.paginator.getLeft(),
            this.paginator.getPaginationLabel(),
            this.paginator.getRight()
        );

        updatePagination(tableSession);
    }

    public HBox getTableFooter() {return this.tableFooter;}

    public void updatePagination(TableSession tableSession) {
        this.paginator.updatePaginationLabel(tableSession.getPaginationData());
        this.paginator.getLeft().setDisable(!tableSession.hasPrevious());
        this.paginator.getRight().setDisable(!tableSession.hasNext());
    }
}
