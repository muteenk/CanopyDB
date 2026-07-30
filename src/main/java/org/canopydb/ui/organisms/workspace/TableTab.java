package org.canopydb.ui.organisms.workspace;

import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.CellValue;
import org.canopydb.models.TableSession;
import org.canopydb.ui.molecules.TabFilterArea;
import org.canopydb.ui.molecules.TableTabFooter;
import org.canopydb.ui.utils.TableComponent;

import java.util.List;

public class TableTab {
    private final Tab tab;
    private TabFilterArea filterArea;
    private final TableView<List<CellValue>> tableView;
    private TableTabFooter footer;

    private TableSession tableSession;
    private final TableViewEventController tableViewEventController;

    public TableTab(TableSession tableSession, TableViewEventController tableViewEventController) {
        this.tableSession = tableSession;
        this.tableViewEventController = tableViewEventController;

        this.tableView = TableComponent.buildTableComponent(tableSession.getTableData());
        tableView.setSortPolicy(tv -> {
            if (tv.getSortOrder().isEmpty()) return true;
            TableColumn<List<CellValue>, ?> selectedColumn = tv.getSortOrder().getFirst();
            String orderBy = selectedColumn.getText();
            tableSession.setQueryOrder(orderBy);
            tableViewEventController.tableReRender(tableSession);
            return true;
        });

        this.tab = buildTab();
    }

    public Tab getTab() {
        return this.tab;
    }

    private Tab buildTab() {
        Tab tab = new Tab(tableSession.getTablePath());
        tab.setContent(buildTabContent());
        return tab;
    }

    private VBox buildTabContent() {
        VBox tableBox = new VBox();

        this.filterArea = new TabFilterArea(tableSession, tableViewEventController);
        this.footer = new TableTabFooter(tableSession, tableViewEventController);

        tableBox.getChildren().addAll(
                this.filterArea.getFilterArea(),
                tableView,
                this.footer.getTableFooter()
        );

        VBox.setVgrow(tableView, Priority.ALWAYS);
        return tableBox;
    }

    public void updateSession(TableSession updatedSession) {
        this.tableSession = updatedSession;
        TableComponent.updateTableContents(this.tableSession.getTableData(), tableView);
        this.footer.updatePagination(this.tableSession);
    }
}
