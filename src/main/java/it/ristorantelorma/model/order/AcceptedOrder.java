package it.ristorantelorma.model.order;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.ristorantelorma.model.Food;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.user.ClientUser;
import it.ristorantelorma.model.user.DeliverymanUser;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represent an entry in the ORDERS table of the database with state = State.ACCEPTED.
 */
@SuppressFBWarnings(
    value = "EQ_DOESNT_OVERRIDE_EQUALS",
    justification = "The added fields do not contribute to the identification of the object"
)
public class AcceptedOrder extends ReadyOrder {

    private final Timestamp acceptanceTime;
    private final DeliverymanUser deliveryman;

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
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "The deliveryman's credit can be mutated without issues"
    )
    public AcceptedOrder(
        final int id,
        final Restaurant restaurant,
        final Timestamp dateTime,
        final BigDecimal shippingRate,
        final ClientUser client,
        final Map<Food, Integer> foodRequested,
        final Timestamp acceptanceTime,
        final DeliverymanUser deliveryman
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
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "The deliveryman's credit can be mutated without issues"
    )
    public AcceptedOrder(
        final ReadyOrder order,
        final Timestamp acceptanceTime,
        final DeliverymanUser deliveryman
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
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "The deliveryman's credit can be mutated without issues"
    )
    public DeliverymanUser getDeliveryman() {
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
            final DeliverymanUser deliveryman
        ) {
            final Result<Order> res = Order.DAO.updateState(
                connection,
                order,
                State.ACCEPTED,
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

        /**
         * List orders ready to be delivered.
         * @param connection
         * @return a Collection<AcceptedOrder> if there are no errors
         * @throws IllegalStateException if the Order searched exists but the linked Restaurant name,
         *         client username, or deliveryman username do not.
         * @throws IllegalArgumentException if an invalid Status enum is returned from the query
         */
        public static Result<Collection<AcceptedOrder>> list(
            final Connection connection
        ) {
            final Result<Collection<Order>> res = Order.DAO.listByState(
                connection,
                State.ACCEPTED
            );
            if (!res.isSuccess()) {
                // Propagate error
                return Result.failure(res.getErrorMessage());
            }
            return Result.success(
                res
                    .getValue()
                    .stream()
                    .map(val -> (AcceptedOrder) val)
                    .collect(Collectors.toSet())
            );
        }
    }
}
