package org.canopydb.controllers;

import javafx.application.Platform;
import org.canopydb.models.TableSession;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.interfaces.PushNotification;
import org.canopydb.ui.interfaces.TableUpdateAction;
import org.canopydb.ui.molecules.Notification;

public class TableViewEventController {
    TableActionService tableActionService = new TableActionService();
    private final TableUpdateAction tableUpdateAction;

    public TableViewEventController(TableUpdateAction tableUpdateAction){
        this.tableUpdateAction = tableUpdateAction;
    }

    public void tableReRender(
            TableSession tableSession
    ) {
        tableActionService.loadTableDataAsync(tableSession)
                .thenAccept(session -> {
                    Platform.runLater(() -> {
                        tableUpdateAction.render(session);
                    });
                }).exceptionally(error -> {
                    Platform.runLater(() -> {
                        System.out.println(error.getMessage());
                        Notification.pushNotification(
                                "Failed to reload table !",
                                error.getMessage(),
                                Notification.NotificationType.DANGER
                        );
                    });
                    return null;
                });
    }
}
