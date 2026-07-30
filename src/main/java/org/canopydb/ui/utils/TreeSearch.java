package org.canopydb.ui.utils;

import javafx.scene.control.TreeItem;
import org.canopydb.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Case-insensitive search over already-loaded connection tree nodes (DB → tables).
 */
public final class TreeSearch {

    private TreeSearch() {
    }

    public record Match(TreeItem<String> database, List<TreeItem<String>> tables) {
    }

    public static boolean isPlaceholder(String value) {
        return value == null
                || value.equals(Constants.LOADING)
                || value.equals(Constants.FAILED)
                || value.equals("Connection");
    }

    public static boolean matches(String text, String normalizedQuery) {
        if (text == null || normalizedQuery == null || normalizedQuery.isEmpty()) {
            return false;
        }
        if (isPlaceholder(text)) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    public static String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        return query.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns loaded tables under a DB node (excludes LOADING / FAILED placeholders).
     */
    public static List<TreeItem<String>> loadedTables(TreeItem<String> database) {
        List<TreeItem<String>> tables = new ArrayList<>();
        if (database == null || database.getChildren().isEmpty()) {
            return tables;
        }
        String first = database.getChildren().getFirst().getValue();
        if (Constants.LOADING.equals(first) || Constants.FAILED.equals(first)) {
            return tables;
        }
        for (TreeItem<String> child : database.getChildren()) {
            if (!isPlaceholder(child.getValue())) {
                tables.add(child);
            }
        }
        return tables;
    }

    public static boolean hasLoadedTables(TreeItem<String> database) {
        return !loadedTables(database).isEmpty();
    }

    /**
     * Walk loaded DBs/tables. DB name match → all loaded tables;
     * otherwise only matching table names (parent DB still included).
     */
    public static List<Match> findLoadedMatches(
            List<TreeItem<String>> databases,
            String query
    ) {
        String normalized = normalizeQuery(query);
        List<Match> matches = new ArrayList<>();
        if (normalized.isEmpty() || databases == null) {
            return matches;
        }

        for (TreeItem<String> database : databases) {
            if (database == null || isPlaceholder(database.getValue())) {
                continue;
            }

            boolean dbMatches = matches(database.getValue(), normalized);
            List<TreeItem<String>> loaded = loadedTables(database);

            List<TreeItem<String>> tablesToShow = new ArrayList<>();
            if (dbMatches) {
                tablesToShow.addAll(loaded);
            } else {
                for (TreeItem<String> table : loaded) {
                    if (matches(table.getValue(), normalized)) {
                        tablesToShow.add(table);
                    }
                }
            }

            if (dbMatches || !tablesToShow.isEmpty()) {
                matches.add(new Match(database, tablesToShow));
            }
        }

        return matches;
    }
}
