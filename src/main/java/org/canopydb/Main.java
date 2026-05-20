package org.canopydb;

import javafx.application.Application;
import javafx.stage.Stage;
import java.util.*;

import org.canopydb.config.ThreadPool;
import org.canopydb.ui.Renderer;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Renderer renderer = new Renderer();
        stage.setTitle("CanopyDB - SQL Client");
        stage.setScene(renderer.render());
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        ThreadPool.getExecutor().shutdown();
        super.stop();
    }

    static void main(String[] args) {
        launch(args);
    }
}
