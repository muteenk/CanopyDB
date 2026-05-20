package org.canopydb.controllers;

import javafx.application.Platform;
import org.canopydb.services.TableActionService;

public class TableViewEventController {
    TableActionService tableActionService = new TableActionService();

    public void tableReRender(
            String tableName,
            String databaseName,
            String filters,
            String ordering,
            String pagination
    ) {
        tableActionService.loadTableDataAsync(tableName, databaseName)
                .thenAccept(table -> {
                    Platform.runLater(() -> {

                    });
                }).exceptionally(error -> {
                    Platform.runLater(() -> {

                    });
                    return null;
                });
    }
}
