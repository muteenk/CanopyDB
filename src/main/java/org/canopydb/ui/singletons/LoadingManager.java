package org.canopydb.ui.singletons;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import org.canopydb.ui.atoms.GlobalProgressBar;

/**
 * App-wide loading strip. Supports overlapping work via a ref-count:
 * the bar stays visible until every {@link #start()} has a matching {@link #stop()}.
 * The last {@link #stop()} sweeps the bar to 100% before hiding.
 */
public final class LoadingManager {

    private static final GlobalProgressBar progressBar = new GlobalProgressBar();
    private static int activeLoads = 0;

    private LoadingManager() {
    }

    public static ProgressBar getNode() {
        return progressBar.getNode();
    }

    /** Begin an indeterminate global load. Safe to call from any thread. */
    public static void start() {
        runOnFx(() -> {
            activeLoads++;
            if (activeLoads == 1) {
                progressBar.showIndeterminate();
            }
        });
    }

    /**
     * End one global load. When the last load finishes, sweeps to 100% then hides.
     */
    public static void stop() {
        runOnFx(() -> {
            activeLoads = Math.max(0, activeLoads - 1);
            if (activeLoads == 0) {
                progressBar.completeAndHide();
            }
        });
    }

    /**
     * Show a determinate 0–1 value and ensure the bar is visible.
     * Does not change the ref-count — pair with {@link #start()}/{@link #stop()}
     * when the operation begins/ends, or call {@link #stop()} when done.
     */
    public static void setProgress(double progress) {
        runOnFx(() -> {
            if (activeLoads == 0) {
                activeLoads = 1;
            }
            progressBar.showProgress(progress);
        });
    }

    /** Force-hide and reset the ref-count (e.g. after a fatal error). */
    public static void reset() {
        runOnFx(() -> {
            activeLoads = 0;
            progressBar.hide();
        });
    }

    private static void runOnFx(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
