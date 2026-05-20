package org.canopydb.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.canopydb.ui.organisms.MiddlePane;
import org.canopydb.ui.organisms.Sidebar;

import java.util.Objects;

public class Renderer {
    private Parent build() {
        MiddlePane middle = new MiddlePane();
        Sidebar sidebar = new Sidebar(middle::addTable);
        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(sidebar.getSidebar());
        borderPane.setCenter(middle.getMiddle());

        return borderPane;
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
