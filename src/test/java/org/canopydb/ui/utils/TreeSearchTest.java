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
        assertFalse(TreeSearch.matches("Connection", "conn")); // root placeholder ignored
    }

    @Test
    void findLoadedMatches_dbNameMatch_includesAllLoadedTables() {
        LazyTreeItem db = loadedDbWithTables("analytics", "events", "users");
        List<TreeSearch.Match> matches = TreeSearch.findLoadedMatches(List.of(db), "analy");

        assertEquals(1, matches.size());
        assertEquals(2, matches.getFirst().tables().size());
    }

    @Test
    void findLoadedMatches_tableNameMatch_includesParentDb() {
        LazyTreeItem db = loadedDbWithTables("app", "orders", "users");
        List<TreeSearch.Match> matches = TreeSearch.findLoadedMatches(List.of(db), "user");

        assertEquals(1, matches.size());
        assertEquals("app", matches.getFirst().database().getValue());
        assertEquals(1, matches.getFirst().tables().size());
        assertEquals("users", matches.getFirst().tables().getFirst().getValue());
    }

    @Test
    void findLoadedMatches_skipsUnloadedDatabaseChildren() {
        LazyTreeItem db = new LazyTreeItem("app");

        assertTrue(TreeSearch.loadedTables(db).isEmpty());
        assertFalse(TreeSearch.hasLoadedTables(db));
        assertEquals(1, TreeSearch.findLoadedMatches(List.of(db), "app").size());
        assertTrue(TreeSearch.findLoadedMatches(List.of(db), "app").getFirst().tables().isEmpty());
    }

    @Test
    void lazyTreeItem_unloadedIsNotLeaf_loadedEmptyIsLeaf() {
        LazyTreeItem db = new LazyTreeItem("app");
        assertFalse(db.isLeaf());
        assertTrue(db.needsLoad());

        db.markLoaded();
        assertTrue(db.isLeaf());
        assertTrue(db.isLoaded());
    }

    private static LazyTreeItem loadedDbWithTables(String dbName, String... tables) {
        LazyTreeItem db = new LazyTreeItem(dbName);
        for (String table : tables) {
            db.getChildren().add(new TreeItem<>(table));
        }
        db.markLoaded();
        return db;
    }
}
