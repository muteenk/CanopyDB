package org.canopydb.ui.utils;

import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lesson: even UI helpers often have pure logic you can test without opening a window.
 * TreeItem can be built in memory; we never show a Stage.
 */
class TreeSearchTest {

    @Test
    void normalizeQuery_trimsAndLowercases() {
        assertEquals("users", TreeSearch.normalizeQuery("  UsErS  "));
        assertEquals("", TreeSearch.normalizeQuery(null));
    }

    @Test
    void matches_isCaseInsensitiveContains() {
        assertTrue(TreeSearch.matches("Users", "user"));
        assertFalse(TreeSearch.matches("Orders", "user"));
        assertFalse(TreeSearch.matches("LOADING", "load")); // placeholder ignored
    }

    @Test
    void findLoadedMatches_dbNameMatch_includesAllLoadedTables() {
        TreeItem<String> db = dbWithTables("analytics", "events", "users");
        List<TreeSearch.Match> matches = TreeSearch.findLoadedMatches(List.of(db), "analy");

        assertEquals(1, matches.size());
        assertEquals(2, matches.getFirst().tables().size());
    }

    @Test
    void findLoadedMatches_tableNameMatch_includesParentDb() {
        TreeItem<String> db = dbWithTables("app", "orders", "users");
        List<TreeSearch.Match> matches = TreeSearch.findLoadedMatches(List.of(db), "user");

        assertEquals(1, matches.size());
        assertEquals("app", matches.getFirst().database().getValue());
        assertEquals(1, matches.getFirst().tables().size());
        assertEquals("users", matches.getFirst().tables().getFirst().getValue());
    }

    @Test
    void findLoadedMatches_skipsLoadingPlaceholderChildren() {
        TreeItem<String> db = new TreeItem<>("app");
        db.getChildren().add(new TreeItem<>("LOADING"));

        assertTrue(TreeSearch.findLoadedMatches(List.of(db), "app").getFirst().tables().isEmpty());
        assertFalse(TreeSearch.hasLoadedTables(db));
    }

    private static TreeItem<String> dbWithTables(String dbName, String... tables) {
        TreeItem<String> db = new TreeItem<>(dbName);
        for (String table : tables) {
            db.getChildren().add(new TreeItem<>(table));
        }
        return db;
    }
}
