package org.canopydb.controllers;

import javafx.application.Platform;
import org.canopydb.config.AppLogger;
import org.canopydb.models.QueryResult;
import org.canopydb.services.AsyncQuery;
import org.canopydb.services.QueryActionService;
import org.canopydb.ui.singletons.LoadingManager;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.utils.ExceptionMessages;
import org.canopydb.utils.QueryExceptions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs / cancels ad-hoc SQL for a single query tab.
 */
public class QueryEditorController {
    private static final Logger LOGGER = AppLogger.getLogger(QueryEditorController.class);

    private final QueryActionService queryActionService = new QueryActionService();
    private final Consumer<QueryResult> onSuccess;
    private final Consumer<String> onFailure;
    private final Runnable onCancelled;
    private final Runnable onBusyChanged;

    private AsyncQuery<QueryResult> inFlight;
    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);
    private boolean running;

    public QueryEditorController(
            Consumer<QueryResult> onSuccess,
            Consumer<String> onFailure,
            Runnable onCancelled,
            Runnable onBusyChanged
    ) {
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
        this.onCancelled = onCancelled;
        this.onBusyChanged = onBusyChanged;
    }

    public boolean isRunning() {
        return running;
    }

    public void run(String sql) {
        String trimmed = sql == null ? "" : sql.trim();
        if (trimmed.isEmpty()) {
            NotificationManager.pushNotification(
                    "Nothing to run",
                    "Enter a SQL statement first.",
                    NotificationManager.NotificationType.INFO
            );
            return;
        }

        cancel();

        cancelRequested.set(false);
        setRunning(true);
        LoadingManager.start();

        AsyncQuery<QueryResult> query = queryActionService.executeAsync(trimmed);
        inFlight = query;

        query.future()
                .whenComplete((result, error) -> {
                    LoadingManager.stop();
                    Platform.runLater(() -> {
                        if (inFlight != query) {
                            return;
                        }
                        inFlight = null;
                        setRunning(false);

                        if (wasCancelled(error)) {
                            if (onCancelled != null) {
                                onCancelled.run();
                            }
                        }
                    });
                })
                .thenAccept(result -> Platform.runLater(() -> {
                    if (cancelRequested.get() || inFlight != null) {
                        return;
                    }
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                }))
                .exceptionally(error -> {
                    if (wasCancelled(error)) {
                        return null;
                    }
                    Platform.runLater(() -> {
                        if (cancelRequested.get()) {
                            return;
                        }
                        LOGGER.log(Level.WARNING, "Query failed", error);
                        String message = ExceptionMessages.userMessage(error);
                        if (onFailure != null) {
                            onFailure.accept(message);
                        }
                        NotificationManager.pushNotification(
                                "Query failed",
                                message,
                                NotificationManager.NotificationType.DANGER
                        );
                    });
                    return null;
                });
    }

    public void cancel() {
        if (inFlight != null) {
            cancelRequested.set(true);
            inFlight.cancel();
        }
    }

    private boolean wasCancelled(Throwable error) {
        return cancelRequested.get() || QueryExceptions.isCancellation(error);
    }

    private void setRunning(boolean value) {
        running = value;
        if (onBusyChanged != null) {
            onBusyChanged.run();
        }
    }
}
