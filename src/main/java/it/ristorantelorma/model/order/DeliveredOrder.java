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
 * Represent an entry in the ORDERS table of the database with state = State.DELIVERED.
 */
@SuppressFBWarnings(
    value = "EQ_DOESNT_OVERRIDE_EQUALS",
    justification = "The added fields do not contribute to the identification of the object"
)
public final class DeliveredOrder extends AcceptedOrder {

    private final Timestamp deliveryTime;

    /**
     * @param id                the ID of the record in the database
     * @param restaurant        the Restaurant to which the order was placed
     * @param dateTime          when the order was created
     * @param shippingRate      the price for the shipment
     * @param client            the client that placed the order
     * @param foodRequested     the food requested by the client
     * @param acceptanceTime
     * @param deliveryman
     * @param deliveryTime
     */
    public DeliveredOrder(
        final int id,
        final Restaurant restaurant,
        final Timestamp dateTime,
        final BigDecimal shippingRate,
        final User client,
        final Map<Food, Integer> foodRequested,
        final Timestamp acceptanceTime,
        final User deliveryman,
        final Timestamp deliveryTime
    ) {
        super(
            id,
            restaurant,
            dateTime,
            shippingRate,
            client,
            foodRequested,
            acceptanceTime,
            deliveryman
        );
        this.deliveryTime = new Timestamp(deliveryTime.getTime());
    }

    /**
     * @param order
     * @param deliveryTime
     */
    public DeliveredOrder(
        final AcceptedOrder order,
        final Timestamp deliveryTime
    ) {
        super(
            order.getId(),
            order.getRestaurant(),
            order.getDateTime(),
            order.getShippingRate(),
            order.getClient(),
            order.getFoodRequested(),
            order.getAcceptanceTime(),
            order.getDeliveryman()
        );
        this.deliveryTime = new Timestamp(deliveryTime.getTime());
    }

    /**
     * @return when the order was delivered
     */
    public Timestamp getDeliveryTime() {
        return (Timestamp) deliveryTime.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getState() {
        return State.DELIVERED;
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
         * @param deliveryTime
         * @return AcceptedOrder if everything goes right during the conversion, error otherwise
         */
        public static Result<AcceptedOrder> from(
            final Connection connection,
            final AcceptedOrder order,
            final Timestamp deliveryTime
        ) {
            final Result<Order> res = Order.DAO.updateState(
                connection,
                order,
                State.DELIVERED,
                deliveryTime
            );
            if (!res.isSuccess()) {
                // Propagate error
                return Result.failure(res.getErrorMessage());
            }
            return Result.success(new DeliveredOrder(order, deliveryTime));
        }
    }
}
