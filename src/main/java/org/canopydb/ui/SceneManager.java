package org.canopydb.ui;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;


public class SceneManager {
    StackPane sceneStack = new StackPane();

    public void setScene(Parent scene) {
        this.sceneStack.getChildren().clear();
        this.sceneStack.getChildren().add(scene);
    }

    public StackPane getScene() {return this.sceneStack;}

}
