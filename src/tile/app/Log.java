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

    /**
     * Prints an error message with red ERROR prefix
     * @param message The error message to print
     */
    public static void error(String message) {
        System.err.println(RED + "ERROR: " + RESET + message);
        Program.setError();
    }

    /**
     * Prints a warning message with yellow WARNING prefix
     * @param message The warning message to print
     */
    public static void warning(String message) {
        System.out.println(YELLOW + "WARNING: " + RESET + message);
    }

    /**
     * Prints a debug message with blue DEBUG prefix if debug mode is enabled
     * @param message The debug message to print
     */
    public static void debug(String message) {
        if (isDebugMode) {
            System.out.println(BLUE + "DEBUG: " + RESET + message);
        }
    }

    /**
     * Prints a regular info message without prefix
     * @param message The info message to print
     */
    public static void info(String message) {
        System.out.println(message);
    }
}