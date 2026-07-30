package org.canopydb.ui.singletons;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class NotificationManager {
    public enum NotificationType {
        INFO,
        SUCCESS,
        DANGER
    }

    private static final int TOAST_MAX_WIDTH = 360;
    private static final int TOAST_MESSAGE_MAX_CHARS = 180;
    private static final Duration DEFAULT_DISPLAY = Duration.seconds(3);
    private static final Duration DANGER_DISPLAY = Duration.seconds(12);

    public static VBox notificationContainer = new VBox(10);

    static {
        notificationContainer.getStyleClass()
                .add("notification-container");
        // Must receive clicks so toasts can open the details modal.
        notificationContainer.setMouseTransparent(false);
        notificationContainer.setFillWidth(false);
        notificationContainer.setAlignment(Pos.TOP_RIGHT);
        notificationContainer.setPickOnBounds(false);
        notificationContainer.setMaxWidth(Region.USE_PREF_SIZE);
        notificationContainer.setMaxHeight(Region.USE_PREF_SIZE);
    }

    private static void setNotificationStyle(
            VBox notification,
            Label titleLabel,
            Label messageLabel,
            NotificationType notificationType
    ) {
        notification.getStyleClass().add("notification");
        switch (notificationType) {
            case SUCCESS ->
                    notification.getStyleClass()
                            .add("notification-success");
            case DANGER ->
                    notification.getStyleClass()
                            .add("notification-danger");
            case INFO ->
                    notification.getStyleClass()
                            .add("notification-info");
        }
        titleLabel.getStyleClass()
                .add("notification-title");
        messageLabel.getStyleClass()
                .add("notification-message");
    }

    private static Duration displayDuration(NotificationType type) {
        return type == NotificationType.DANGER ? DANGER_DISPLAY : DEFAULT_DISPLAY;
    }

    private static void setNotificationAnimations(
            VBox notification,
            NotificationType notificationType
    ) {
        FadeTransition fadeIn = new FadeTransition(
                Duration.millis(200),
                notification
        );
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition wait = new PauseTransition(displayDuration(notificationType));

        FadeTransition fadeOut = new FadeTransition(
                Duration.millis(300),
                notification
        );
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(
                fadeIn, wait, fadeOut
        );
        sequence.setOnFinished(event ->
                notificationContainer
                        .getChildren()
                        .remove(notification)
        );
        notification.setUserData(sequence);
        sequence.play();
    }

    private static void stopNotificationAnimation(VBox notification) {
        Object data = notification.getUserData();
        if (data instanceof SequentialTransition sequence) {
            sequence.stop();
        }
    }

    public static void pushNotification(
            String title,
            String message,
            NotificationType notificationType
    ) {
        String safeTitle = title == null ? "" : title;
        String safeMessage = message == null ? "" : message;

        VBox notification = new VBox(6);
        Label titleLabel = new Label(safeTitle);
        Label messageLabel = new Label(truncateForToast(safeMessage));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(TOAST_MAX_WIDTH - 36);

        notification.setMaxWidth(TOAST_MAX_WIDTH);
        notification.setCursor(javafx.scene.Cursor.HAND);
        setNotificationStyle(
                notification,
                titleLabel,
                messageLabel,
                notificationType
        );

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        header.getChildren().add(titleLabel);

        if (notificationType == NotificationType.DANGER) {
            Button copyButton = new Button("Copy");
            copyButton.getStyleClass().add("notification-copy-button");
            copyButton.setOnAction(e -> {
                e.consume();
                copyToClipboard(safeTitle, safeMessage);
            });
            // Prevent toast click-to-modal when pressing Copy.
            copyButton.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
            header.getChildren().add(copyButton);
        }

        notification.getChildren().addAll(header, messageLabel);
        notification.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button) {
                return;
            }
            stopNotificationAnimation(notification);
            notificationContainer.getChildren().remove(notification);
            showDetailsModal(safeTitle, safeMessage, notificationType);
        });

        notificationContainer.getChildren().add(notification);
        setNotificationAnimations(notification, notificationType);
    }

    private static String truncateForToast(String message) {
        String normalized = message.replace('\n', ' ').trim();
        if (normalized.length() <= TOAST_MESSAGE_MAX_CHARS) {
            return normalized;
        }
        return normalized.substring(0, TOAST_MESSAGE_MAX_CHARS).trim() + "…";
    }

    private static void copyToClipboard(String title, String message) {
        String payload = title.isBlank() ? message : title + "\n\n" + message;
        ClipboardContent content = new ClipboardContent();
        content.putString(payload);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private static void showDetailsModal(
            String title,
            String message,
            NotificationType notificationType
    ) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle(title.isBlank() ? "Notification" : title);

        Label heading = new Label(title);
        heading.getStyleClass().add("notification-modal-title");
        heading.setWrapText(true);

        TextArea details = new TextArea(message);
        details.setEditable(false);
        details.setWrapText(true);
        details.getStyleClass().add("notification-modal-message");
        VBox.setVgrow(details, Priority.ALWAYS);

        Button copyButton = new Button("Copy");
        copyButton.getStyleClass().add("notification-modal-button");
        copyButton.setOnAction(e -> copyToClipboard(title, message));

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("notification-modal-button");
        closeButton.setDefaultButton(true);
        closeButton.setOnAction(e -> dialog.close());

        HBox actions = new HBox(10, copyButton, closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("notification-modal-actions");

        VBox root = new VBox(14, heading, details, actions);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("notification-modal");
        if (notificationType == NotificationType.DANGER) {
            root.getStyleClass().add("notification-modal-danger");
        } else if (notificationType == NotificationType.SUCCESS) {
            root.getStyleClass().add("notification-modal-success");
        } else {
            root.getStyleClass().add("notification-modal-info");
        }

        Scene scene = new Scene(root, 560, 360);
        if (notificationContainer.getScene() != null) {
            scene.getStylesheets().addAll(notificationContainer.getScene().getStylesheets());
        }
        dialog.setScene(scene);
        dialog.setMinWidth(420);
        dialog.setMinHeight(280);
        dialog.showAndWait();
    }
}
