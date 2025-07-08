package it.ristorantelorma.model;


/**
 * This class contains all the SQL queries required.
 */
public final class Queries {
    /**
     * Find a User based of its username.
     */
    public static final String FIND_USER =
        """
        SELECT * FROM UTENTI WHERE username = ?;
        """;

    /**
     * Insert a new User.
     */
    public static final String INSERT_USER =
        """
        INSERT INTO UTENTI
        (nome, cognome, username, password, telefono, email,
        città, via, n_civico, credito, ruolo)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

    private Queries() {
        throw new UnsupportedOperationException("Utility class and cannot be instantiated");
    }
}
