package org.canopydb.ui.atoms;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

/**
 * Thin full-width progress strip used for global loading feedback.
 */
public final class GlobalProgressBar {

    private static final Duration SWEEP_DURATION = Duration.millis(280);
    private static final Duration HOLD_AT_END = Duration.millis(140);
    private static final double SWEEP_START_WHEN_INDETERMINATE = 0.12;

    private final ProgressBar progressBar = new ProgressBar();
    private Timeline sweepTimeline;
    private PauseTransition holdAtEnd;

    public GlobalProgressBar() {
        progressBar.getStyleClass().add("global-progress");
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setMinHeight(3);
        progressBar.setPrefHeight(3);
        progressBar.setMaxHeight(3);
        progressBar.setProgress(0);
        hide();
    }

    public ProgressBar getNode() {
        return progressBar;
    }

    public void showIndeterminate() {
        cancelFinishAnimation();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setVisible(true);
        progressBar.setManaged(true);
    }

    public void showProgress(double progress) {
        cancelFinishAnimation();
        progressBar.setProgress(Math.clamp(progress, 0, 1));
        progressBar.setVisible(true);
        progressBar.setManaged(true);
    }

    /**
     * Animate to 100%, briefly hold, then hide.
     * Call when the last in-flight load completes.
     */
    public void completeAndHide() {
        cancelFinishAnimation();

        progressBar.setVisible(true);
        progressBar.setManaged(true);

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
        progressBar.setVisible(false);
        progressBar.setManaged(false);
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
