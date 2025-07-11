package it.ristorantelorma.model.order;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.ristorantelorma.model.Food;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.User;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Map;

/**
 * Represent an entry in the ORDERS table of the database with state = State.ACCEPTED.
 */
@SuppressFBWarnings(
    value = "EQ_DOESNT_OVERRIDE_EQUALS",
    justification = "The added fields do not contribute to the identification of the object"
)
public class AcceptedOrder extends ReadyOrder {

    private final Timestamp acceptanceTime;
    private final User deliveryman;

    /**
     * @param id                the ID of the record in the database
     * @param restaurant        the Restaurant to which the order was placed
     * @param dateTime          when the order was created
     * @param shippingRate      the price for the shipment
     * @param client            the client that placed the order
     * @param foodRequested     the food requested by the client
     * @param acceptanceTime
     * @param deliveryman
     */
    public AcceptedOrder(
        final int id,
        final Restaurant restaurant,
        final Timestamp dateTime,
        final BigDecimal shippingRate,
        final User client,
        final Map<Food, Integer> foodRequested,
        final Timestamp acceptanceTime,
        final User deliveryman
    ) {
        super(id, restaurant, dateTime, shippingRate, client, foodRequested);
        this.acceptanceTime = new Timestamp(acceptanceTime.getTime());
        this.deliveryman = deliveryman;
    }

    /**
     * @param order
     * @param acceptanceTime
     * @param deliveryman
     */
    public AcceptedOrder(
        final ReadyOrder order,
        final Timestamp acceptanceTime,
        final User deliveryman
    ) {
        super(
            order.getId(),
            order.getRestaurant(),
            order.getDateTime(),
            order.getShippingRate(),
            order.getClient(),
            order.getFoodRequested()
        );
        this.acceptanceTime = new Timestamp(acceptanceTime.getTime());
        this.deliveryman = deliveryman;
    }

    /**
     * @return when the order was accepted
     */
    public Timestamp getAcceptanceTime() {
        return (Timestamp) acceptanceTime.clone();
    }

    /**
     * @return the User of the deliveryman
     */
    public User getDeliveryman() {
        return deliveryman;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getState() {
        return State.ACCEPTED;
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
         * @param acceptanceTime
         * @param deliveryman
         * @return AcceptedOrder if everything goes right during the conversion, error otherwise
         */
        public static Result<AcceptedOrder> from(
            final Connection connection,
            final ReadyOrder order,
            final Timestamp acceptanceTime,
            final User deliveryman
        ) {
            final Result<Order> res = Order.DAO.updateState(
                connection,
                order,
                State.READY,
                acceptanceTime,
                deliveryman.getUsername()
            );
            if (!res.isSuccess()) {
                // Propagate error
                return Result.failure(res.getErrorMessage());
            }
            return Result.success(
                new AcceptedOrder(order, acceptanceTime, deliveryman)
            );
        }
    }
}
