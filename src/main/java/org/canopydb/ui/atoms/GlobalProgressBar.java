package org.canopydb.ui.atoms;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Thin full-width progress strip used for global loading feedback.
 * The host keeps a fixed height so idle / loading never shifts the layout.
 */
public final class GlobalProgressBar {

    public static final double BAR_HEIGHT = 3;

    private static final Duration SWEEP_DURATION = Duration.millis(280);
    private static final Duration HOLD_AT_END = Duration.millis(140);
    private static final double SWEEP_START_WHEN_INDETERMINATE = 0.12;

    private final StackPane host = new StackPane();
    private final ProgressBar progressBar = new ProgressBar();
    private Timeline sweepTimeline;
    private PauseTransition holdAtEnd;

    public GlobalProgressBar() {
        progressBar.getStyleClass().add("global-progress");
        progressBar.setMaxSize(Double.MAX_VALUE, BAR_HEIGHT);
        progressBar.setMinHeight(BAR_HEIGHT);
        progressBar.setPrefHeight(BAR_HEIGHT);
        progressBar.setMaxHeight(BAR_HEIGHT);
        progressBar.setProgress(0);

        host.getStyleClass().add("global-progress-host");
        host.setMinHeight(BAR_HEIGHT);
        host.setPrefHeight(BAR_HEIGHT);
        host.setMaxHeight(BAR_HEIGHT);
        host.setMaxWidth(Double.MAX_VALUE);
        host.getChildren().add(progressBar);
    }

    public Region getNode() {
        return host;
    }

    public void showIndeterminate() {
        cancelFinishAnimation();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
    }

    public void showProgress(double progress) {
        cancelFinishAnimation();
        progressBar.setProgress(Math.clamp(progress, 0, 1));
    }

    /**
     * Animate to 100%, briefly hold, then return to idle (progress 0).
     * Call when the last in-flight load completes.
     */
    public void completeProgress() {
        cancelFinishAnimation();

        double from = progressBar.getProgress();
        if (from < 0) {
            // Indeterminate → start the sweep from a small determinate value.
            from = SWEEP_START_WHEN_INDETERMINATE;
        }
        progressBar.setProgress(from);

        sweepTimeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(progressBar.progressProperty(), from)
                ),
                new KeyFrame(
                        SWEEP_DURATION,
                        new KeyValue(
                                progressBar.progressProperty(),
                                1.0,
                                Interpolator.EASE_OUT
                        )
                )
        );
        sweepTimeline.setOnFinished(e -> {
            holdAtEnd = new PauseTransition(HOLD_AT_END);
            holdAtEnd.setOnFinished(ev -> hide());
            holdAtEnd.play();
        });
        sweepTimeline.play();
    }

    public void hide() {
        cancelFinishAnimation();
        progressBar.setProgress(0);
    }

    private void cancelFinishAnimation() {
        if (sweepTimeline != null) {
            sweepTimeline.stop();
            sweepTimeline = null;
        }
        if (holdAtEnd != null) {
            holdAtEnd.stop();
            holdAtEnd = null;
        }
    }
}
