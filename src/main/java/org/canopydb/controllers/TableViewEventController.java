package org.canopydb.controllers;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import org.canopydb.queries.Order;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.interfaces.TableOpenAction;
import org.canopydb.ui.interfaces.TableUpdateAction;

import java.util.List;

public class TableViewEventController {
    TableActionService tableActionService = new TableActionService();
    private final TableUpdateAction tableUpdateAction;

    public TableViewEventController(TableUpdateAction tableUpdateAction){
        this.tableUpdateAction = tableUpdateAction;
    }

    public void tableReRender(
            String tableName,
            String databaseName,
            String orderBy,
            Order.OrderDirection orderDirection,
            TableView<List<String>> tableView
    ) {
        tableActionService.loadTableDataAsync(tableName, databaseName, orderBy, orderDirection)
                .thenAccept(table -> {
                    Platform.runLater(() -> {
                        tableUpdateAction.render(table, tableView);
                    });
                }).exceptionally(error -> {
                    Platform.runLater(() -> {
                        // TODO: HANDLE TABLE LOADING FAILURE
                    });
                    return null;
                });
    }
}
