package org.canopydb.ui.molecules;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.layout.StackPane;

public class Notification {

    private final VBox notificationContainer;

    public Notification() {

        notificationContainer = new VBox(10);

        notificationContainer.getStyleClass()
                .add("notification-container");

        notificationContainer.setMouseTransparent(true);

        notificationContainer.setFillWidth(false);

        notificationContainer.setAlignment(Pos.TOP_RIGHT);

        notificationContainer.setPickOnBounds(false);

        notificationContainer.setMaxWidth(Region.USE_PREF_SIZE);

        notificationContainer.setMaxHeight(Region.USE_PREF_SIZE);
    }

    public enum NotificationType {
        INFO,
        SUCCESS,
        DANGER
    }

    public void pushNotification(
            String title,
            String message,
            NotificationType notificationType
    ) {

        VBox notification = new VBox(4);
        notification.setMaxWidth(320);
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

        Label titleLabel = new Label(title);

        titleLabel.getStyleClass()
                .add("notification-title");

        Label messageLabel = new Label(message);

        messageLabel.getStyleClass()
                .add("notification-message");

        notification.getChildren()
                .addAll(titleLabel, messageLabel);

        notificationContainer
                .getChildren()
                .add(notification);

        FadeTransition fadeIn =
                new FadeTransition(
                        Duration.millis(200),
                        notification
                );

        fadeIn.setFromValue(0);

        fadeIn.setToValue(1);

        PauseTransition wait =
                new PauseTransition(
                        Duration.seconds(3)
                );

        FadeTransition fadeOut =
                new FadeTransition(
                        Duration.millis(300),
                        notification
                );

        fadeOut.setFromValue(1);

        fadeOut.setToValue(0);

        SequentialTransition sequence =
                new SequentialTransition(
                        fadeIn,
                        wait,
                        fadeOut
                );

        sequence.setOnFinished(event ->
                notificationContainer
                        .getChildren()
                        .remove(notification)
        );

        sequence.play();
    }

    public VBox getContainer() {
        return notificationContainer;
    }
}