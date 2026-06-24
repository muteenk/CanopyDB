package org.canopydb.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import org.canopydb.ui.interfaces.View;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.ui.views.ConnectionView;
import org.canopydb.ui.singletons.ViewManager;

import java.util.Objects;

public class Renderer {
    private Parent build() {
        StackPane root = new StackPane();

        View initScene = new ConnectionView();
        ViewManager.pushView(initScene.getView());

        root.getChildren().addAll(
                ViewManager.viewStack,
                NotificationManager.notificationContainer
        );
        StackPane.setAlignment(
                NotificationManager.notificationContainer,
                Pos.BOTTOM_RIGHT
        );

        return root;
    }

    public javafx.scene.Scene render() {
        javafx.scene.Scene scene = new javafx.scene.Scene(build(), 1280, 720);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/style.css")
                ).toExternalForm()
        );
        return scene;
    }
}
