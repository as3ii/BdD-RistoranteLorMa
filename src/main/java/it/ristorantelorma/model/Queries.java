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
     * Set the credit of a User.
     */
    public static final String SET_USER_CREDIT =
        """
        UPDATE UTENTI
        SET credito = ?
        WHERE usename = ?;
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
     * Find the most purchased FoodType.
     */
    public static final String FIND_FOOD_TYPE_MOST_BUYED =
        """
        SELECT t.*,SUM(d.quantità) AS "totale"
        FROM TIPO_VIVANDE t, VIVANDE v, DETTAGLIO_ORDINI d
        WHERE t.nome = v.tipologia AND v.codice = d.codice_vivanda
        GROUP BY t.nome ORDER BY totale DESC LIMIT 1;
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

    /**
     * List foodIds and quantities for the given Order ID.
     */
    public static final String LIST_FOODS_BY_ORDER_ID =
        """
        SELECT * FROM DETTAGLIO_ORDINI
        WHERE codice_ordine = ?;
        """;

    /**
     * Insert a new record in DETTAGLIO_ORDINI.
     */
    public static final String INSERT_ORDER_DETAIL =
        """
        INSERT INTO DETTAGLIO_ORDINI
        (codice_vivanda, codice_ordine, quantità)
        """;

    /**
     * Find an Order by its ID.
     */
    public static final String FIND_ORDER_BY_ID =
        """
        SELECT * FROM ORDINI
        WHERE id = ?;
        """;

    /**
     * Insert a new order, with Status.WAITING and optional fields set to null.
     */
    public static final String INSERT_ORDER =
        """
        INSERT INTO ORDINI
        (nome_attività, data_ora, stato, tariffa_spedizione, username_cliente)
        (?, ?, 'attesa', ?, ?)
        """;

    /**
     * Set state to ready for the given order ID.
     */
    public static final String SET_ORDER_READY =
        """
        UPDATE ORDINI
        SET stato = 'pronto'
        WHERE codice = ?;
        """;

    /**
     * Set state to accepted and the acceptance time and deliveryman for the given order ID.
     */
    public static final String SET_ORDER_ACCEPTED =
        """
        UPDATE ORDINI
        SET stato = 'accettato', ora_accettazione = ?, username_fattorino = ?
        WHERE codice = ?;
        """;

    /**
     * Set state to delivered and set the delivery time for the given order ID.
     */
    public static final String SET_ORDER_DELIVERED =
        """
        UPDATE ORDINI
        SET stato = 'pronto', ora_consegna = ?
        WHERE codice = ?;
        """;

    /**
     * Set state to cancelled for the given order ID.
     */
    public static final String SET_ORDER_CANCELLED =
        """
        UPDATE ORDINI
        SET stato = 'annullato'
        WHERE codice = ?;
        """;

    /**
     * List all reviews for the given restaurant name.
     */
    public static final String LIST_REVIEWS_OF_RESTAURANT =
        """
        SELECT * FROM RECENSIONI
        WHERE nome_attività = ?;
        """;

    /**
     * Insert a new Review.
     */
    public static final String INSERT_REVIEW =
        """
        INSERT INTO RECENSIONI
        (nome_attività, data, voto, commento, username)
        VALUES (?, ?, ?, ?, ?);
        """;

    /**
     * Delete a review given its ID.
     */
    public static final String DELETE_REVIEW =
        """
        DELETE FROM RECENSIONI
        WHERE codice = ?;
        """;

    private Queries() {
        throw new UnsupportedOperationException("Utility class and cannot be instantiated");
    }
}
