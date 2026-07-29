package org.canopydb.config;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Simple, dependency-free logger wrapper for CanopyDB.
 *
 * Uses {@link java.util.logging} so we don't introduce new external dependencies.
 */
public final class AppLogger {

    private static final String BASE_LOGGER_NAME = "org.canopydb";
    private static final String LEVEL_PROPERTY = "canopy.log.level";

    private static final Level DEFAULT_LEVEL = Level.INFO;

    private static volatile boolean configured = false;

    private AppLogger() {
    }

    public static Logger getLogger(Class<?> clazz) {
        ensureConfigured();
        return Logger.getLogger(clazz.getName());
    }

    private static void ensureConfigured() {
        if (configured) return;
        synchronized (AppLogger.class) {
            if (configured) return;

            Level level = readLevel();

            // Configure the JUL root logger so all children inherit the console handler.
            Logger root = Logger.getLogger("");
            root.setLevel(level);

            boolean hasConsoleHandler =
                    Arrays.stream(root.getHandlers())
                            .anyMatch(ConsoleHandler.class::isInstance);

            if (!hasConsoleHandler) {
                ConsoleHandler handler = new ConsoleHandler();
                handler.setLevel(Level.ALL);
                handler.setFormatter(new SimpleLogFormatter());
                root.addHandler(handler);
            }

            // Make sure our base namespace defaults to the same level.
            Logger base = Logger.getLogger(BASE_LOGGER_NAME);
            base.setLevel(level);

            configured = true;
        }
    }

    private static Level readLevel() {
        String raw = System.getProperty(LEVEL_PROPERTY);
        if (raw == null || raw.isBlank()) return DEFAULT_LEVEL;

        try {
            return Level.parse(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return DEFAULT_LEVEL;
        }
    }

    private static final class SimpleLogFormatter extends Formatter {
        @Override
        public String format(LogRecord logRecord) {
            String loggerName = logRecord.getLoggerName();
            String simpleName = loggerName;

            int lastDot = loggerName.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < loggerName.length() - 1) {
                simpleName = loggerName.substring(lastDot + 1);
            }

            String msg = formatMessage(logRecord);
            return String.format(
                    "%1$tF %1$tT [%2$s] %3$s - %4$s%n",
                    logRecord.getMillis(),
                    logRecord.getLevel().getName(),
                    simpleName,
                    msg
            );
        }
    }
}

