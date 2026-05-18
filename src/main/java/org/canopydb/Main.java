package org.canopydb;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        StackPane stackPane = new StackPane();
        Scene scene = new Scene(stackPane, 350, 230,
        Color.LIGHTYELLOW);

        stage.setTitle("CanopyDB - Lite SQL Client");
        stage.setScene(scene);
        stage.show();
    }

    static void main() {
        launch();
    }
}
