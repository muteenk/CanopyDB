package org.canopydb;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.util.*;

import org.canopydb.ui.organisms.MiddlePane;
import org.canopydb.ui.organisms.Sidebar;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        MiddlePane middle = new MiddlePane();
        Sidebar sidebar = new Sidebar(middle::addTable);
        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(sidebar.getSidebar());
        borderPane.setCenter(middle.getMiddle());

        Scene scene = new Scene(borderPane, 1280, 720);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/style.css")
                ).toExternalForm()
        );
        stage.setTitle("CanopyDB - SQL Client");
        stage.setScene(scene);
        stage.show();
    }

    static void main(String[] args) {
        launch(args);
    }
}
