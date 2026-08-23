package org.canopydb.utils;

import org.canopydb.models.ConnectionLabel;
import org.canopydb.models.ConnectionMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ClientStateManager owns JSON file I/O under the state directory.
 * Feature code (e.g. ConnectionManager) supplies defaults and domain rules.
 */
class ClientStateManagerTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void redirectStateRoot() {
        ClientStateManager.setStateRootForTests(tempDir);
    }

    @AfterEach
    void restoreStateRoot() {
        ClientStateManager.clearStateRootForTests();
    }

    @Test
    void stateFilePath_resolvesUnderStateDirectory() {
        Path path = ClientStateManager.stateFilePath("connections.json");

        assertEquals(tempDir.resolve("connections.json"), path);
    }

    @Test
    void exists_returnsFalseWhenFileMissing() {
        assertFalse(ClientStateManager.exists("missing.json"));
    }

    @Test
    void write_createsDirectoryAndFile() throws IOException {
        ClientStateManager.write("settings.json", Map.of("theme", "dark"));

        Path file = tempDir.resolve("settings.json");
        assertTrue(Files.exists(file));
        assertTrue(Files.readString(file).contains("\"theme\" : \"dark\""));
    }

    @Test
    void readList_roundTripsConnectionList() throws IOException {
        List<ConnectionMeta> connections = List.of(
                new ConnectionMeta(
                        "Local",
                        "localhost",
                        3306,
                        "root",
                        "secret",
                        "app",
                        ConnectionLabel.LOCAL
                )
        );

        ClientStateManager.write(Constants.CONNECTIONS_STATE_FILE, connections);

        List<ConnectionMeta> loaded = ClientStateManager.readList(
                Constants.CONNECTIONS_STATE_FILE,
                ConnectionMeta.class
        );

        assertEquals(1, loaded.size());
        assertEquals("Local", loaded.getFirst().getName());
        assertEquals("localhost", loaded.getFirst().getHost());
        assertEquals(3306, loaded.getFirst().getPort());
        assertEquals("root", loaded.getFirst().getUsername());
        assertEquals("secret", loaded.getFirst().getPassword());
        assertEquals("app", loaded.getFirst().getDatabase());
        assertEquals(ConnectionLabel.LOCAL, loaded.getFirst().getLabel());
    }

    @Test
    void read_roundTripsSingleObject() throws IOException {
        Map<String, String> prefs = Map.of("lastConnectionId", "abc-123");

        ClientStateManager.write("preferences.json", prefs);

        @SuppressWarnings("unchecked")
        Map<String, String> loaded = ClientStateManager.read("preferences.json", Map.class);

        assertEquals("abc-123", loaded.get("lastConnectionId"));
    }

    @Test
    void write_overwritesExistingFile() throws IOException {
        ClientStateManager.write("notes.json", List.of("first"));
        ClientStateManager.write("notes.json", List.of("second"));

        List<String> loaded = ClientStateManager.readList("notes.json", String.class);

        assertEquals(List.of("second"), loaded);
    }

    @Test
    void read_throwsWhenFileMissing() {
        IOException error = assertThrows(
                IOException.class,
                () -> ClientStateManager.read("missing.json", Map.class)
        );

        assertTrue(error.getMessage().contains("State file not found"));
    }

    @Test
    void readList_throwsWhenFileMissing() {
        IOException error = assertThrows(
                IOException.class,
                () -> ClientStateManager.readList("missing.json", ConnectionMeta.class)
        );

        assertTrue(error.getMessage().contains("State file not found"));
    }

    @Test
    void exists_returnsTrueAfterWrite() throws IOException {
        ClientStateManager.write("workspace.json", List.of("analytics"));

        assertTrue(ClientStateManager.exists("workspace.json"));
    }
}
