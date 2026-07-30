package org.canopydb.controllers;

import javafx.application.Platform;
import org.canopydb.models.TableSession;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.interfaces.TableUpdateAction;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.config.AppLogger;
import org.canopydb.utils.ExceptionMessages;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TableViewEventController {
    TableActionService tableActionService = new TableActionService();
    private final TableUpdateAction tableUpdateAction;
    private static final Logger LOGGER = AppLogger.getLogger(TableViewEventController.class);

    public TableViewEventController(TableUpdateAction tableUpdateAction){
        this.tableUpdateAction = tableUpdateAction;
    }

    public void tableReRender(
            TableSession tableSession
    ) {
        tableActionService.loadTableDataAsync(tableSession)
                .thenAccept(session -> {
                    Platform.runLater(() -> tableUpdateAction.render(session));
                }).exceptionally(error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(
                                Level.WARNING,
                                "Failed to reload table",
                                error
                        );
                        NotificationManager.pushNotification(
                                "Failed to reload table !",
                                ExceptionMessages.userMessage(error),
                                NotificationManager.NotificationType.DANGER
                        );
                    });
                    return null;
                });
    }
}
