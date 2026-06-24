package org.canopydb.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import org.canopydb.ui.interfaces.Scene;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.ui.scenes.ConnectionScene;
import org.canopydb.ui.singletons.SceneManager;

import java.util.Objects;

public class Renderer {
    private Parent build() {
        StackPane root = new StackPane();

        Scene initScene = new ConnectionScene();
        SceneManager.pushScene(initScene.getScene());

        root.getChildren().addAll(
                SceneManager.sceneStack,
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
