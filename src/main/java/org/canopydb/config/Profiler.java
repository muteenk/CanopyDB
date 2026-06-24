package org.canopydb.config;

public class Profiler {
    public static void logMemory() {
        Runtime runtime = Runtime.getRuntime();

        long used = runtime.totalMemory() - runtime.freeMemory();

        System.out.printf(
                "Memory: %.2f MB%n",
                used / (1024.0 * 1024)
        );
    }
}
