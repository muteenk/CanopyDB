package org.canopydb.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.canopydb.ui.molecules.Notification;
import org.canopydb.ui.scenes.WorkspaceScene;

import java.util.Objects;

public class Renderer {
    private Parent build() {
        StackPane root = new StackPane();

        Notification notification = new Notification();
        SceneManager sceneManager = new SceneManager();

        WorkspaceScene workspaceScene = new WorkspaceScene(notification);
        sceneManager.setScene(workspaceScene.getScene());

        root.getChildren().addAll(
                sceneManager.getScene(),
                notification.getContainer()
        );
        StackPane.setAlignment(
                notification.getContainer(),
                Pos.BOTTOM_RIGHT
        );

        return root;
    }

    public Scene render() {
        Scene scene = new Scene(build(), 1280, 720);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/style.css")
                ).toExternalForm()
        );
        return scene;
    }
}
