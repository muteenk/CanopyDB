package org.canopydb.utils;

/**
 * Turns nested async/SQL exceptions into a short message suitable for UI notifications.
 */
public final class ExceptionMessages {

    private ExceptionMessages() {
    }

    public static String userMessage(Throwable error) {
        if (error == null) {
            return "Unknown error";
        }

        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        String message = root.getMessage();
        if (message == null || message.isBlank()) {
            return root.getClass().getSimpleName();
        }

        // Some wrappers put "com.example.FooException: actual message" in getMessage().
        return stripLeadingExceptionClass(message.trim());
    }

    private static String stripLeadingExceptionClass(String message) {
        int colon = message.indexOf(':');
        if (colon <= 0 || colon >= message.length() - 1) {
            return message;
        }
        String prefix = message.substring(0, colon);
        if (prefix.contains(".") && prefix.endsWith("Exception")) {
            return message.substring(colon + 1).trim();
        }
        return message;
    }
}
