package org.canopydb.ui.utils;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;

/**
 * Tree node that can expand before children exist, and shows an inline loader while fetching.
 * <p>
 * Unloaded nodes report {@code isLeaf() == false} so the disclosure arrow appears without a
 * LOADING/FAILED placeholder child. On failure, callers should {@link #markUnloaded()} and collapse.
 */
public final class LazyTreeItem extends TreeItem<String> {

    public enum LoadState {
        UNLOADED,
        LOADING,
        LOADED
    }

    private LoadState loadState = LoadState.UNLOADED;

    public LazyTreeItem(String value) {
        super(value);
    }

    public LoadState getLoadState() {
        return loadState;
    }

    public boolean needsLoad() {
        return loadState == LoadState.UNLOADED;
    }

    public boolean isLoading() {
        return loadState == LoadState.LOADING;
    }

    public boolean isLoaded() {
        return loadState == LoadState.LOADED;
    }

    public void beginLoading() {
        loadState = LoadState.LOADING;
        setGraphic(createLoader());
    }

    public void markLoaded() {
        loadState = LoadState.LOADED;
        setGraphic(null);
    }

    /** Clears children and returns to unloaded so the next expand retries. */
    public void markUnloaded() {
        loadState = LoadState.UNLOADED;
        setGraphic(null);
        getChildren().clear();
    }

    @Override
    public boolean isLeaf() {
        if (loadState != LoadState.LOADED) {
            return false;
        }
        return super.isLeaf();
    }

    private static ProgressIndicator createLoader() {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefSize(12, 12);
        indicator.setMaxSize(12, 12);
        indicator.getStyleClass().add("tree-node-loader");
        return indicator;
    }
}
