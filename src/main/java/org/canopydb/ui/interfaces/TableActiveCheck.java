package org.canopydb.ui.interfaces;

@FunctionalInterface
public interface TableActiveCheck {
    boolean isActive(String table);
}
