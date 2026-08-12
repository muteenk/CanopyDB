package org.canopydb.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import org.canopydb.ui.interfaces.View;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.ui.views.ConnectionView;
import org.canopydb.ui.singletons.ViewManager;

import java.util.List;
import java.util.Objects;

public class Renderer {

    /** Load order matters when rules overlap — keep base first. */
    private static final List<String> STYLESHEETS = List.of(
            "/styles/base.css",
            "/styles/topbar.css",
            "/styles/sidebar.css",
            "/styles/inputs.css",
            "/styles/tree.css",
            "/styles/tabs.css",
            "/styles/table.css",
            "/styles/scrollbars.css",
            "/styles/notifications.css",
            "/styles/dialogs.css",
            "/styles/pagination.css",
            "/styles/filters.css",
            "/styles/connections.css"
    );

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
        for (String path : STYLESHEETS) {
            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            getClass().getResource(path),
                            "Missing stylesheet: " + path
                    ).toExternalForm()
            );
        }
        return scene;
    }
}
