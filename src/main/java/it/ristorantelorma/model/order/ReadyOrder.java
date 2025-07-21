package it.ristorantelorma.model.order;

import it.ristorantelorma.model.Food;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.user.ClientUser;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represent an entry in the ORDERS table of the database with state = State.READY.
 */
public class ReadyOrder extends WaitingOrder {

    /**
     * @param id                the ID of the record in the database
     * @param restaurant        the Restaurant to which the order was placed
     * @param dateTime          when the order was created
     * @param shippingRate      the price for the shipment
     * @param client            the client that placed the order
     * @param foodRequested     the food requested by the client
     */
    public ReadyOrder(
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
     * @param order
     */
    public ReadyOrder(final WaitingOrder order) {
        super(
            order.getId(),
            order.getRestaurant(),
            order.getDateTime(),
            order.getShippingRate(),
            order.getClient(),
            order.getFoodRequested()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getState() {
        return State.READY;
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

        /**
         * @param connection
         * @param order
         * @return ReadyOrder if everything goes right during the conversion, error otherwise
         */
        public static Result<ReadyOrder> from(
            final Connection connection,
            final WaitingOrder order
        ) {
            final Result<Order> res = Order.DAO.updateState(
                connection,
                order,
                State.READY
            );
            if (!res.isSuccess()) {
                // Propagate error
                return Result.failure(res.getErrorMessage());
            }
            return Result.success(new ReadyOrder(order));
        }

        /**
         * List orders ready to be delivered.
         * @param connection
         * @return a Collection<ReadyOrder> if there are no errors
         * @throws IllegalStateException if the Order searched exists but the linked Restaurant name,
         *         client username, or deliveryman username do not.
         * @throws IllegalArgumentException if an invalid Status enum is returned from the query
         */
        public static Result<Collection<ReadyOrder>> list(
            final Connection connection
        ) {
            final Result<Collection<Order>> res = Order.DAO.listByState(
                connection,
                State.READY
            );
            if (!res.isSuccess()) {
                // Propagate error
                return Result.failure(res.getErrorMessage());
            }
            return Result.success(
                res
                    .getValue()
                    .stream()
                    .map(val -> (ReadyOrder) val)
                    .collect(Collectors.toSet())
            );
        }
    }
}
