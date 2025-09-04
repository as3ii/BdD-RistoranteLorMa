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
        SELECT * FROM utenti WHERE username = ?;
        """;

    /**
     * Insert a new User.
     */
    public static final String INSERT_USER =
        """
        INSERT INTO utenti
        (nome, cognome, username, password, telefono, email,
        città, via, n_civico, credito, ruolo)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

    /**
     * Set the credit of a User.
     */
    public static final String SET_USER_CREDIT =
        """
        UPDATE utenti
        SET credito = ?
        WHERE username = ?;
        """;

    /**
     * Find the deliveryman with more deliveries.
     */
    public static final String FIND_DELIVERYMAN_WITH_MORE_DELIVERIES =
        """
        SELECT u.*,COUNT(o.username_fattorino) AS "numero_ordini"
        FROM utenti u, ordini o
        WHERE u.username = o.username_fattorino AND ora_consegna IS NOT NULL
        GROUP BY o.username_fattorino ORDER BY numero_ordini DESC LIMIT 1;
        """;
    /**
     * Find a Restaurant by its name.
     */
    public static final String FIND_RESTAURANT_BY_NAME =
        """
        SELECT * FROM ristoranti WHERE nome_attività = ?;
        """;

    /**
     * Find a Restaurant by its owner username.
     */
    public static final String FIND_RESTAURANT_BY_USERNAME =
        """
        SELECT * FROM ristoranti WHERE username = ?;
        """;

    /**
     * Insert a new Restaurant.
     */
    public static final String INSERT_RESTAURANT =
        """
        INSERT INTO ristoranti
        (username, nome_attività, p_iva, ora_apertura, ora_chiusura)
        VALUES (?, ?, ?, ?, ?);
        """;

    /**
     * List all Restaurants.
     */
    public static final String LIST_RESTAURANTS =
        """
        SELECT * FROM ristoranti;
        """;

    /**
     * Find the Restaurant with the most orders.
     */
    public static final String FIND_RESTAURANT_MOST_ORDERS =
        """
        SELECT r.*,COUNT(o.nome_attività) AS "numero_ordini"
        FROM ristoranti r, ordini o
        WHERE r.nome_attività = o.nome_attività
        GROUP BY o.nome_attività ORDER BY numero_ordini DESC LIMIT 1;
        """;

    /**
     * Find the Restaurant with the most negative reviews.
     */
    public static final String FIND_RESTAURANT_MOST_NEGATIVE_REVIEWS =
        """
        SELECT ris.*,AVG(CAST(CAST(rec.voto AS CHAR) AS INT)) AS average
        FROM ristoranti ris, recensioni rec
        WHERE ris.nome_attività = rec.nome_attività
        GROUP BY rec.nome_attività ORDER BY average ASC LIMIT 1;
        """;

    /**
     * Update opening and closing time.
     */
    public static final String UPDATE_RESTAURANT =
        """
        UPDATE ristoranti
        SET ora_apertura = ?, ora_chiusura = ?
        WHERE nome_attività = ?;
        """;

    /**
     * Find a FoodType by its name.
     */
    public static final String FIND_FOOD_TYPE =
        """
        SELECT * FROM tipo_vivande WHERE nome = ?;
        """;

    /**
     * Insert a new FoodType.
     */
    public static final String INSERT_FOOD_TYPE =
        """
        INSERT INTO tipo_vivande
        (nome, tipologia)
        VALUES (?, ?);
        """;

    /**
     * List all FoodTypes.
     */
    public static final String LIST_FOOD_TYPES =
        """
        SELECT * FROM tipo_vivande;
        """;

    /**
     * Find the most purchased FoodType.
     */
    public static final String FIND_FOOD_TYPE_MOST_PURCHASED =
        """
        SELECT t.*,SUM(d.quantità) AS "totale"
        FROM tipo_vivande t, vivande v, dettaglio_ordini d
        WHERE t.nome = v.tipologia AND v.codice = d.codice_vivanda
        GROUP BY t.nome ORDER BY totale DESC LIMIT 1;
        """;

    /**
     * Find a Food based on its name and the Restaurant name.
     */
    public static final String FIND_FOOD_BY_NAME =
        """
        SELECT * FROM vivande
        WHERE nome = ? AND nome_attività = ?;
        """;

    /**
     * Find a Food based on its ID.
     */
    public static final String FIND_FOOD_BY_ID =
        """
        SELECT * FROM vivande
        WHERE codice = ?;
        """;

    /**
     * Insert a new Food.
     */
    public static final String INSERT_FOOD =
        """
        INSERT INTO vivande
        (nome, nome_attività, prezzo, tipologia)
        VALUES (?, ?, ?, ?);
        """;

    /**
     * List all foods of a Restaurant.
     */
    public static final String LIST_FOODS =
        """
        SELECT * FROM vivande
        WHERE nome_attività = ?;
        """;

    /**
     * List foodIds and quantities for the given Order ID.
     */
    public static final String LIST_FOODS_BY_ORDER_ID =
        """
        SELECT * FROM dettaglio_ordini
        WHERE codice_ordine = ?;
        """;

    /**
     * Find the most purchased Food.
     */
    public static final String FIND_FOOD_MOST_PURCHASED =
        """
        SELECT v.*,SUM(d.quantità) AS "quantità_totale"
        FROM vivande v, dettaglio_ordini d
        WHERE v.codice = d.codice_vivanda
        GROUP BY codice_vivanda ORDER BY quantità_totale DESC LIMIT 1;
        """;

    /**
     * Update name/price/FoodType.
     */
    public static final String UPDATE_FOOD =
        """
        UPDATE vivande
        SET nome = ?, prezzo = ?, tipologia = ?
        WHERE codice = ?;
        """;

    /**
     * Delete the food.
     */
    public static final String DELETE_FOOD =
        """
        DELETE FROM vivande WHERE codice = ?;
        """;

    /**
     * Insert a new record in dettaglio_ordini.
     */
    public static final String INSERT_ORDER_DETAIL =
        """
        INSERT INTO dettaglio_ordini
        (codice_vivanda, codice_ordine, quantità)
        VALUES (?, ?, ?);
        """;

    /**
     * Find an Order by its ID.
     */
    public static final String FIND_ORDER_BY_ID =
        """
        SELECT * FROM ordini
        WHERE id = ?;
        """;

    /**
     * Insert a new order, with Status.WAITING and optional fields set to null.
     */
    public static final String INSERT_ORDER =
        """
        INSERT INTO ordini
        (nome_attività, data_ora, stato, tariffa_spedizione, username_cliente)
        VALUES (?, ?, 'attesa', ?, ?);
        """;

    /**
     * Set state to ready for the given order ID.
     */
    public static final String SET_ORDER_READY =
        """
        UPDATE ordini
        SET stato = 'pronto'
        WHERE codice = ?;
        """;

    /**
     * Set state to accepted and the acceptance time and deliveryman for the given order ID.
     */
    public static final String SET_ORDER_ACCEPTED =
        """
        UPDATE ordini
        SET stato = 'accettato', ora_accettazione = ?, username_fattorino = ?
        WHERE codice = ?;
        """;

    /**
     * Set state to delivered and set the delivery time for the given order ID.
     */
    public static final String SET_ORDER_DELIVERED =
        """
        UPDATE ordini
        SET stato = 'consegnato', ora_consegna = ?
        WHERE codice = ?;
        """;

    /**
     * Set state to cancelled for the given order ID.
     */
    public static final String SET_ORDER_CANCELLED =
        """
        UPDATE ordini
        SET stato = 'annullato'
        WHERE codice = ?;
        """;

    /**
     * Lists orders with the given state.
     */
    public static final String LIST_ORDERS_BY_STATE =
        """
        SELECT * FROM ordini
        WHERE stato = ?;
        """;

    /**
     * List all reviews for the given restaurant name.
     */
    public static final String LIST_REVIEWS_OF_RESTAURANT =
        """
        SELECT * FROM recensioni
        WHERE nome_attività = ?;
        """;

    /**
     * Insert a new Review.
     */
    public static final String INSERT_REVIEW =
        """
        INSERT INTO recensioni
        (nome_attività, data, voto, commento, username)
        VALUES (?, ?, ?, ?, ?);
        """;

    /**
     * Delete a review given its ID.
     */
    public static final String DELETE_REVIEW =
        """
        DELETE FROM recensioni
        WHERE codice = ?;
        """;

    private Queries() {
        throw new UnsupportedOperationException("Utility class and cannot be instantiated");
    }
}
