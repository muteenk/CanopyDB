package org.canopydb.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.canopydb.config.AppLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * Control point for JSON state files under {@code ~/.canopydb/}.
 * Feature code owns defaults and domain logic; this class only handles I/O.
 */
public final class ClientStateManager {

    private static final Logger LOGGER = AppLogger.getLogger(ClientStateManager.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Redirects state I/O to a temp directory during tests. */
    static Path stateRootOverride;

    private ClientStateManager() {
    }

    static void setStateRootForTests(Path root) {
        stateRootOverride = root;
    }

    static void clearStateRootForTests() {
        stateRootOverride = null;
    }

    public static Path stateDirectory() {
        if (stateRootOverride != null) {
            return stateRootOverride;
        }
        return Path.of(System.getProperty(Constants.HOME_PATH), Constants.DIRECTORY);
    }

    public static Path stateFilePath(String filename) {
        return stateDirectory().resolve(filename);
    }

    public static boolean exists(String filename) {
        return Files.exists(stateFilePath(filename));
    }

    public static void ensureDirectory() throws IOException {
        Files.createDirectories(stateDirectory());
    }

    public static <T> void write(String filename, T data) throws IOException {
        ensureDirectory();
        Path path = stateFilePath(filename);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);
        LOGGER.fine(() -> "Wrote state file: " + path.toAbsolutePath());
    }

    public static <T> T read(String filename, Class<T> type) throws IOException {
        Path path = stateFilePath(filename);
        if (!Files.exists(path)) {
            throw new IOException("State file not found: " + path.toAbsolutePath());
        }
        return MAPPER.readValue(path.toFile(), type);
    }

    public static <T> List<T> readList(String filename, Class<T> elementClass) throws IOException {
        Path path = stateFilePath(filename);
        if (!Files.exists(path)) {
            throw new IOException("State file not found: " + path.toAbsolutePath());
        }
        return MAPPER.readValue(
                path.toFile(),
                MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass)
        );
    }
}
