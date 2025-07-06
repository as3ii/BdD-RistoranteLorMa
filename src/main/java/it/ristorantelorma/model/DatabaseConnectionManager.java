package it.ristorantelorma.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.ristorantelorma.controller.SimpleLogger;

/**
 * Handle database connections and DataSource.
 */
public final class DatabaseConnectionManager {
    private final Connection connection;
    private final String className = getClass().getName();
    private final Logger logger = SimpleLogger.getLogger(className);

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_DBNAME = "APP_RISTORANTI";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static final class LazyConnectionManager {
        private static final DatabaseConnectionManager DB = new DatabaseConnectionManager();
    }

    private DatabaseConnectionManager() {
        final Map<String, String> env = System.getenv();

        final String hostname = env.getOrDefault("DB_HOSTNAME", DEFAULT_HOSTNAME);
        int port;
        try {
            port = Integer.parseUnsignedInt(
                env.getOrDefault("DB_PORT", String.valueOf(DEFAULT_PORT))
            );
        } catch (NumberFormatException e) {
            port = DEFAULT_PORT;
        }
        final String dbName = env.getOrDefault("DB_NAME", DEFAULT_DBNAME);
        final String user = env.getOrDefault("DB_USER", DEFAULT_USER);
        final String password = env.getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);

        final String url = String.format("jdbc:mysql://%s:%i/%s", hostname, port, dbName);

        try {
            logger.log(Level.INFO, "Connecting to " + url);
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Cannot create a connection to the database", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
    * @return copy of a DatabaseConnectionManager
    */
    public static DatabaseConnectionManager getInstance() {
        return LazyConnectionManager.DB;
    }

    /**
     * @return copy of a connection
     * TODO: switch to a DriverManager
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "This is a demo, not production code")
    public Connection getConnection() {
        return connection;
    }
}
