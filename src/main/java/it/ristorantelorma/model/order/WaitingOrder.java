package it.ristorantelorma.model.order;

import it.ristorantelorma.model.Food;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.user.ClientUser;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

/**
 * Represent an entry in the ORDERS table of the database with state = State.WAITING.
 */
public class WaitingOrder extends Order {

    /**
     * @param id                the ID of the record in the database
     * @param restaurant        the Restaurant to which the order was placed
     * @param dateTime          when the order was created
     * @param shippingRate      the price for the shipment
     * @param client            the client that placed the order
     * @param foodRequested     the food requested by the client
     */
    public WaitingOrder(
        final int id,
        final Restaurant restaurant,
        final Timestamp dateTime,
        final BigDecimal shippingRate,
        final ClientUser client,
        final Map<Food, Integer> foodRequested
    ) {
        super(id, restaurant, dateTime, shippingRate, client, foodRequested);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getState() {
        return State.WAITING;
    }

    /**
     * Inner class that handles requests to the database.
     */
    public static final class DAO {

        private DAO() {
            throw new UnsupportedOperationException(
                "Utility class and cannot be instantiated"
            );
        }
    }
}
