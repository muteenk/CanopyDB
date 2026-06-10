package org.canopydb.ui.organisms;

import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TablePagination;
import org.canopydb.models.TableSession;
import org.canopydb.ui.molecules.TableTabFooter;
import org.canopydb.ui.utils.TableComponent;

import java.util.List;

public class TableTab {
    private final TableView<List<String>> tableView;
    private final Tab tab;
    private TableTabFooter footer;

    private final TableViewEventController tableViewEventController;

    public TableTab(TableSession tableSession, TableViewEventController tableViewEventController) {
        this.tableViewEventController = tableViewEventController;

        this.tableView = TableComponent.buildTableComponent(tableSession.getTableData());
        tableView.setSortPolicy(tv -> {
            if (tv.getSortOrder().isEmpty()) return true;
            TableColumn<List<String>, ?> selectedColumn = tv.getSortOrder().getFirst();
            String orderBy = selectedColumn.getText();
            tableSession.setQueryOrder(orderBy);
            tableViewEventController.tableReRender(tableSession);
            return true;
        });

        this.tab = buildTab(tableSession);
    }

    public Tab getTab() {
        return this.tab;
    }

    private Tab buildTab(TableSession tableSession) {
        Tab tab = new Tab(tableSession.getTablePath());
        tab.setContent(buildTabContent(tableSession));
        return tab;
    }

    private VBox buildTabContent(TableSession tableSession) {
        VBox tableBox = new VBox();
        this.footer = new TableTabFooter(tableSession, tableViewEventController);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableBox.getChildren().addAll(tableView, this.footer.getTableFooter());
        return tableBox;
    }

    public void updateSession(TableSession tableSession) {
        TableComponent.updateTableContents(tableSession.getTableData(), tableView);
        this.footer.updatePagination(tableSession);
    }
}
