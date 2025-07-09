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

    /**
     * Find a Restaurant by its name.
     */
    public static final String FIND_RESTAURANT_BY_NAME =
        """
        SELECT * FROM RISTORANTI WHERE nome_attività = ?;
        """;

    /**
     * Find a Restaurant by its owner username.
     */
    public static final String FIND_RESTAURANT_BY_USERNAME =
        """
        SELECT * FROM RISTORANTI WHERE username = ?;
        """;

    /**
     * Insert a new Restaurant.
     */
    public static final String INSERT_RESTAURANT =
        """
        INSERT INTO RISTORANTI
        (username, nome_attività, p_va, ora_apertura, ora_chiusura)
        VALUES (?, ?, ?, ?, ?);
        """;

    /**
     * List all Restaurants.
     */
    public static final String LIST_RESTAURANTS =
        """
        SELECT * FROM RISTORANTI;
        """;

    /**
     * Find a FoodType by its name.
     */
    public static final String FIND_FOOD_TYPE =
        """
        SELECT * FROM TIPO_VIVANDE WHERE nome = ?;
        """;

    /**
     * Insert a new FoodType.
     */
    public static final String INSERT_FOOD_TYPE =
        """
        INSERT INTO TIPO_VIVANDE
        (nome, tipologia)
        VALUES (?, ?);
        """;

    /**
     * List all FoodTypes.
     */
    public static final String LIST_FOOD_TYPES =
        """
        SELECT * FROM TIPO_VIVANDE;
        """;

    /**
     * Find a Food based on its name and the Restaurant name.
     */
    public static final String FIND_FOOD_BY_NAME =
        """
        SELECT * FROM VIVANDE
        WHERE nome = ? AND nome_attività = ?;
        """;

    /**
     * Find a Food based on its ID.
     */
    public static final String FIND_FOOD_BY_ID =
        """
        SELECT * FROM VIVANTE
        WHERE id = ?;
        """;

    /**
     * Insert a new Food.
     */
    public static final String INSERT_FOOD =
        """
        INSERT INTO VIVANDE
        (nome, nome_attività, prezzo, tipologia)
        VALUES (?, ?, ?, ?);
        """;

    /**
     * List all foods of a Restaurant.
     */
    public static final String LIST_FOODS =
        """
        SELECT * FROM VIVANDE
        WHERE nome_attività = ?;
        """;

    private Queries() {
        throw new UnsupportedOperationException("Utility class and cannot be instantiated");
    }
}
