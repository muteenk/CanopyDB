package org.canopydb.ui.singletons;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;


public class SceneManager {
    public static StackPane sceneStack = new StackPane();

    public static void pushScene(Parent scene) {
        sceneStack.getChildren().add(scene);
    }

    public static void popScene() {
        if (sceneStack.getChildren().isEmpty()) return;
        sceneStack.getChildren().removeLast();
    }

    public static void replaceScene(Parent scene) {
        if (!sceneStack.getChildren().isEmpty()) sceneStack.getChildren().removeLast();
        sceneStack.getChildren().add(scene);
    }
}
