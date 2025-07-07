package it.ristorantelorma.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Helper class, used to simplify interaction with the database.
 */
public final class DBHelper {
    private DBHelper() {
        throw new UnsupportedOperationException("Utility class and cannot be instantiated");
    }

    /**
     * Build helper for PreparedStatement.
     * @param connection     A Connection to the DatabaseConnectionManager
     * @param query          Query to be executed
     * @param objects        Objects that will be mapped in the query
     * @return               A PreparedStatement ready to be executed
     * @throws SQLException
     */
    public static PreparedStatement prepare(
        final Connection connection, final String query, final Object... objects
    ) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            for (int i = 0; i < objects.length; i++) {
                statement.setObject(i + 1, objects[i]);
            }
            return statement;
        } catch (SQLException e) {
            if (statement != null) {
                statement.close();
            }
            throw e;
        }
    }
}
