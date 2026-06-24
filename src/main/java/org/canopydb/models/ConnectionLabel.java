package org.canopydb.models;

public enum ConnectionLabel {

    LOCAL("Local", "connection-label-local"),
    DEV("Dev", "connection-label-dev"),
    PROD("Prod", "connection-label-prod"),
    TESTING("Testing", "connection-label-testing");

    private final String displayName;
    private final String styleClass;

    ConnectionLabel(String displayName, String styleClass) {
        this.displayName = displayName;
        this.styleClass = styleClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStyleClass() {
        return styleClass;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
