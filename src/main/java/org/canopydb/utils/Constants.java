package org.canopydb.utils;

public class Constants {
    // FILTER STATES
    public static String APPLY = "Apply";
    public static String APPLIED = "Applied";

    // CLIENT STATE CONSTANTS
    public static String DIRECTORY = ".canopydb";
    public static String HOME_PATH = "user.home";
    public static String CONNECTIONS_STATE_FILE = "connections.json";

    // CONNECTION TIMEOUTS (ms)
    /** MySQL JDBC TCP connect deadline. */
    public static final int JDBC_CONNECT_TIMEOUT_MS = 5_000;
    /** MySQL JDBC socket read deadline (0 = forever). */
    public static final int JDBC_SOCKET_TIMEOUT_MS = 30_000;
    /** Hikari: max wait for a pooled connection. */
    public static final int HIKARI_CONNECTION_TIMEOUT_MS = 10_000;
    /** Hikari: max time for connection validation. */
    public static final int HIKARI_VALIDATION_TIMEOUT_MS = 3_000;
}
