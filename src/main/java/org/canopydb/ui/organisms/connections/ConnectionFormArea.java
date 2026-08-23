package org.canopydb.ui.organisms.connections;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.config.AppLogger;
import org.canopydb.config.DatabasePool;
import org.canopydb.config.ThreadPool;
import org.canopydb.models.ConnectionMeta;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.ui.singletons.ViewManager;
import org.canopydb.ui.views.WorkspaceView;
import org.canopydb.utils.ExceptionMessages;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionFormArea {
    private static final Logger LOGGER = AppLogger.getLogger(ConnectionFormArea.class);

    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox contentArea = new VBox();

    /**
     * Bumped whenever the visible form changes so in-flight test/connect
     * callbacks cannot update a form the user already left.
     */
    private final AtomicInteger formGeneration = new AtomicInteger();
    private ConnectionForm activeForm;

    private Consumer<ConnectionMeta> onSave;

    public ConnectionFormArea() {
        contentArea.getStyleClass().add("connection-content-area");
        contentArea.setAlignment(Pos.CENTER);
        contentArea.setFillWidth(true);
        contentArea.setMinWidth(0);
        contentArea.setMinHeight(0);

        scrollPane.setContent(contentArea);
        scrollPane.getStyleClass().add("connection-content-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMinWidth(0);
        scrollPane.setMinHeight(0);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        showWelcome();
    }

    public ScrollPane getConnectionFormArea() {
        return scrollPane;
    }

    public void setOnSave(Consumer<ConnectionMeta> onSave) {
        this.onSave = onSave;
    }

    public void showWelcome() {
        invalidateActiveForm();
        contentArea.getChildren().setAll(buildWelcome());
    }

    public void showConnectionForm() {
        showConnectionForm(null);
    }

    public void showConnectionForm(ConnectionMeta connection) {
        invalidateActiveForm();
        ConnectionForm form = new ConnectionForm(connection);
        activeForm = form;
        wireFormActions(form);
        contentArea.getChildren().setAll(form.getRoot());
    }

    private void invalidateActiveForm() {
        formGeneration.incrementAndGet();
        activeForm = null;
    }

    private boolean isCurrentForm(ConnectionForm form, int generation) {
        return form == activeForm && generation == formGeneration.get();
    }

    private void wireFormActions(ConnectionForm form) {
        form.setOnTest(connection -> {
            int generation = formGeneration.get();
            form.setBusy(true, "Testing connection…");
            ThreadPool.getExecutor().execute(() -> {
                try {
                    DatabasePool.testConnection(connection);
                    Platform.runLater(() -> {
                        if (!isCurrentForm(form, generation)) {
                            return;
                        }
                        form.setBusy(false, null);
                        NotificationManager.pushNotification(
                                "Connection Test Successful",
                                "Successfully connected to "
                                        + connection.getHost()
                                        + ":"
                                        + connection.getPort(),
                                NotificationManager.NotificationType.SUCCESS
                        );
                    });
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Connection test failed", ex);
                    Platform.runLater(() -> {
                        if (!isCurrentForm(form, generation)) {
                            return;
                        }
                        form.setBusy(false, null);
                        NotificationManager.pushNotification(
                                "Connection Failed",
                                ex.getMessage() != null
                                        ? ExceptionMessages.userMessage(ex)
                                        : "Unable to connect with the provided details.",
                                NotificationManager.NotificationType.DANGER
                        );
                    });
                }
            });
        });

        form.setOnSave(connection -> {
            if (onSave != null) {
                onSave.accept(connection);
            }
            form.markClean();
            NotificationManager.pushNotification(
                    "Connection Saved Successfully",
                    connection.getName() + " was saved successfully.",
                    NotificationManager.NotificationType.SUCCESS
            );
        });

        form.setOnConnect(connection -> {
            int generation = formGeneration.get();
            form.setBusy(true, "Connecting…");
            ThreadPool.getExecutor().execute(() -> {
                try {
                    long epoch = DatabasePool.connect(connection);
                    Platform.runLater(() -> {
                        if (!isCurrentForm(form, generation)) {
                            // User left this form; tear down only if no newer pool replaced us.
                            DatabasePool.disconnectIfEpoch(epoch);
                            return;
                        }

                        if (onSave != null) {
                            onSave.accept(connection);
                        }

                        form.setBusy(false, null);

                        NotificationManager.pushNotification(
                                "Connected and Connection Saved",
                                connection.getName() + " was connected and saved successfully.",
                                NotificationManager.NotificationType.SUCCESS
                        );

                        ViewManager.pushView(new WorkspaceView(connection).getView());
                    });
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Connection failed", ex);
                    Platform.runLater(() -> {
                        if (!isCurrentForm(form, generation)) {
                            return;
                        }
                        form.setBusy(false, null);
                        NotificationManager.pushNotification(
                                "Connection Failed",
                                ExceptionMessages.userMessage(ex),
                                NotificationManager.NotificationType.DANGER
                        );
                    });
                }
            });
        });
    }

    private VBox buildWelcome() {
        Image logoImage = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/assets/logo.png")
                )
        );

        ImageView logo = new ImageView(logoImage);
        logo.setFitWidth(280);
        logo.setPreserveRatio(true);
        logo.setSmooth(true);
        logo.getStyleClass().add("connection-logo");

        Label subtitle = new Label(
                "Manage and connect to multiple databases through a "
                        + "lightweight modern desktop client."
        );
        subtitle.getStyleClass().add("welcome-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(360);
        subtitle.setAlignment(Pos.CENTER);

        VBox welcome = new VBox(28, logo, subtitle);
        welcome.getStyleClass().add("connection-welcome");
        welcome.setAlignment(Pos.CENTER);
        welcome.setMinWidth(0);
        welcome.setMaxWidth(Double.MAX_VALUE);

        return welcome;
    }
}
