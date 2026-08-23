package org.canopydb.controllers;

import javafx.application.Platform;
import org.canopydb.config.AppLogger;
import org.canopydb.models.TableSession;
import org.canopydb.services.AsyncQuery;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.interfaces.TableUpdateAction;
import org.canopydb.ui.singletons.LoadingManager;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.utils.ExceptionMessages;
import org.canopydb.utils.QueryExceptions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TableViewEventController {
    private static final Logger LOGGER = AppLogger.getLogger(TableViewEventController.class);

    private final TableActionService tableActionService = new TableActionService();
    private final TableUpdateAction tableUpdateAction;
    private final Map<String, AsyncQuery<TableSession>> inFlightReloads = new ConcurrentHashMap<>();

    public TableViewEventController(TableUpdateAction tableUpdateAction) {
        this.tableUpdateAction = tableUpdateAction;
    }

    public void tableReRender(TableSession tableSession) {
        String tablePath = tableSession.getTablePath();
        cancelPending(tablePath);

        LoadingManager.start();
        AsyncQuery<TableSession> query = tableActionService.loadTableDataAsync(tableSession);
        inFlightReloads.put(tablePath, query);

        query.future()
                .whenComplete((session, error) -> {
                    LoadingManager.stop();
                    inFlightReloads.remove(tablePath, query);
                })
                .thenAccept(session -> Platform.runLater(() -> tableUpdateAction.render(session)))
                .exceptionally(error -> {
                    if (QueryExceptions.isCancellation(error)) {
                        return null;
                    }
                    Platform.runLater(() -> {
                        LOGGER.log(Level.WARNING, "Failed to reload table", error);
                        NotificationManager.pushNotification(
                                "Failed to reload table !",
                                ExceptionMessages.userMessage(error),
                                NotificationManager.NotificationType.DANGER
                        );
                    });
                    return null;
                });
    }

    /** Cancels an in-flight reload/filter/page fetch for the given tab. */
    public void cancelPending(String tablePath) {
        AsyncQuery<TableSession> existing = inFlightReloads.remove(tablePath);
        if (existing != null) {
            existing.cancel();
        }
    }
}
