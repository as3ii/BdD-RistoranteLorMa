package it.ristorantelorma.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple preconfigured logger.
 */
public final class SimpleLogger {

    /**
     * Private constructor.
     */
    private SimpleLogger() {
        throw new UnsupportedOperationException("Utility class and cannot be instantiated");
    }

    /**
     * Get a preconfigured logger that logs in stderr all INFO or above levels.
     * @param name logger name
     * @return preconfigured Logger
     */
    public static Logger getLogger(final String name) {
        return getLogger(name, Level.INFO);
    }

    /**
     * Get a preconfigured logger that logs in stderr.
     * @param name logger name
     * @param level lowest logged level
     * @return preconfigured Logger
     */
    public static Logger getLogger(final String name, final Level level) {
        final Logger logger = Logger.getLogger(name);
        logger.setLevel(level);
        return logger;
    }
}
