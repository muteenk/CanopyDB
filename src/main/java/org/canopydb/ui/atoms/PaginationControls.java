package org.canopydb.ui.atoms;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.canopydb.models.TablePagination;

public class PaginationControls {
    private final Button left = new Button("◀");
    private final Button right = new Button("▶");
    private final Label paginationLabel = new Label("");

    public PaginationControls(TablePagination pagination) {
        left.getStyleClass().add("pagination-button");
        right.getStyleClass().add("pagination-button");
        paginationLabel.getStyleClass().add("pagination-label");

        updatePaginationLabel(pagination);
    }

    public void updatePaginationLabel(TablePagination pagination) {
        this.paginationLabel.setText(
            ((pagination.totalRows() > 0) ? pagination.offset()+1 : 0) +
            " - " +
            Math.min(pagination.offset() + pagination.limit(), pagination.totalRows()) +
            " of " +
            pagination.totalRows()
        );
    }

    public Button getLeft() {
        return left;
    }

    public Button getRight() {
        return right;
    }

    public Label getPaginationLabel() {
        return paginationLabel;
    }
}
