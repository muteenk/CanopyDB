package org.canopydb.config;

import java.util.logging.Logger;

public class Profiler {
    private static final Logger LOGGER = AppLogger.getLogger(Profiler.class);

    private Profiler() {
    }

    public static void logMemory() {
        Runtime runtime = Runtime.getRuntime();

        long used = runtime.totalMemory() - runtime.freeMemory();

        double usedMb = used / (1024.0 * 1024);
        LOGGER.info(String.format("Memory: %.2f MB", usedMb));
    }
}
