package tile.app;

import tile.Program;

public class Log {
    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";

    // Debug mode flag
    private static boolean isDebugMode = false;

    /**
     * Sets debug mode to enable/disable debug logging
     * @param debugMode true to enable debug logs, false to disable
     */
    public static void setDebugMode(boolean debugMode) {
        isDebugMode = debugMode;
    }

    // --- f-postfix methods ---

    public static String errorf(String message) {
        return RED + "ERROR: " + RESET + message;
    }

    public static String warningf(String message) {
        return YELLOW + "WARNING: " + RESET + message;
    }

    public static String debugf(String message) {
        return BLUE + "DEBUG: " + RESET + message;
    }

    public static String infof(String message) {
        return message;
    }

    // --- printing methods ---

    public static void error(String message) {
        System.err.println(errorf(message));
        Program.setError();
    }

    public static void warning(String message) {
        System.out.println(warningf(message));
    }

    public static void debug(String message) {
        if (isDebugMode) {
            System.out.println(debugf(message));
        }
    }

    public static void info(String message) {
        System.out.println(infof(message));
    }
}