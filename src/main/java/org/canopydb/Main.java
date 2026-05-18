package org.canopydb;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.*;
import org.canopydb.ui.Sidebar;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Sidebar sidebar = new Sidebar();
        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(sidebar.getSidebar());
        borderPane.setStyle("-fx-background-color: #22242a");
        Scene scene = new Scene(borderPane, 1280, 720,
                Color.DARKGRAY);

        stage.setTitle("CanopyDB - SQL Client");
        stage.setScene(scene);
        stage.show();
    }

    static void main(String[] args) {
        launch(args);
    }
}
