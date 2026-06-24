package org.canopydb.ui.singletons;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;


public class ViewManager {
    public static StackPane viewStack = new StackPane();

    public static void pushView(Parent view) {
        viewStack.getChildren().add(view);
    }

    public static void popView() {
        if (viewStack.getChildren().isEmpty()) return;
        viewStack.getChildren().removeLast();
    }

    public static void replaceView(Parent view) {
        if (!viewStack.getChildren().isEmpty()) viewStack.getChildren().removeLast();
        viewStack.getChildren().add(view);
    }
}
