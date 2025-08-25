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
        WHERE username = ?;
        """;

    /**
     * Find the deliveryman with more deliveries.
     */
    public static final String FIND_DELIVERYMAN_WITH_MORE_DELIVERIES =
        """
        SELECT u.*,COUNT(o.username_fattorino) AS "numero_ordini"
        FROM UTENTI u, ORDINI o
        WHERE u.username = o.username_fattorino AND ora_consegna IS NOT NULL
        GROUP BY o.username_fattorino ORDER BY numero_ordini DESC LIMIT 1;
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
        (username, nome_attività, p_iva, ora_apertura, ora_chiusura)
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
     * Find the Restaurant with the most orders.
     */
    public static final String FIND_RESTAURANT_MOST_ORDERS =
        """
        SELECT r.*,COUNT(o.nome_attività) AS "numero_ordini"
        FROM RISTORANTI r, ORDINI o
        WHERE r.nome_attività = o.nome_attività
        GROUP BY o.nome_attività ORDER BY numero_ordini DESC LIMIT 1;
        """;

    /**
     * Find the Restaurant with the most negative reviews.
     */
    public static final String FIND_RESTAURANT_MOST_NEGATIVE_REVIEWS =
        """
        SELECT ris.*,AVG(CAST(CAST(rec.voto AS CHAR) AS INT)) AS average
        FROM RISTORANTI ris, RECENSIONI rec
        WHERE ris.nome_attività = rec.nome_attività
        GROUP BY rec.nome_attività ORDER BY average ASC LIMIT 1;
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
    public static final String FIND_FOOD_TYPE_MOST_PURCHASED =
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
        SELECT * FROM VIVANDE
        WHERE codice = ?;
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
     * Find the most purchased Food.
     */
    public static final String FIND_FOOD_MOST_PURCHASED =
        """
        SELECT v.*,SUM(d.quantità) AS "quantità_totale"
        FROM VIVANDE v, DETTAGLIO_ORDINI d
        WHERE v.codice = d.codice_vivanda
        GROUP BY codice_vivanda ORDER BY quantità_totale DESC LIMIT 1;
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
        SET stato = 'consegnato', ora_consegna = ?
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
     * Lists orders with the given state.
     */
    public static final String LIST_ORDERS_BY_STATE =
        """
        SELECT * FROM ORDINI
        WHERE stato = ?;
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
