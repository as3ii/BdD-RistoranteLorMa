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
import java.util.Optional;

/**
 * Represent an entry in the ORDERS table of the database with state = State.CANCELLED.
 */
@SuppressFBWarnings(
    value = "EQ_DOESNT_OVERRIDE_EQUALS",
    justification = "The added fields do not contribute to the identification of the object"
)
public final class CancelledOrder extends Order {

    private final Optional<Timestamp> acceptanceTime;
    private final Optional<Timestamp> deliveryTime;
    private final Optional<User> deliveryman;

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
    public CancelledOrder(
        final int id,
        final Restaurant restaurant,
        final Timestamp dateTime,
        final BigDecimal shippingRate,
        final User client,
        final Map<Food, Integer> foodRequested,
        final Optional<Timestamp> acceptanceTime,
        final Optional<User> deliveryman,
        final Optional<Timestamp> deliveryTime
    ) {
        super(id, restaurant, dateTime, shippingRate, client, foodRequested);
        if (acceptanceTime.isPresent()) {
            this.acceptanceTime = Optional.of(
                new Timestamp(acceptanceTime.get().getTime())
            );
        } else {
            this.acceptanceTime = Optional.empty();
        }
        this.deliveryman = deliveryman;
        if (deliveryTime.isPresent()) {
            this.deliveryTime = Optional.of(
                new Timestamp(deliveryTime.get().getTime())
            );
        } else {
            this.deliveryTime = Optional.empty();
        }
    }

    /**
     * @param order
     */
    public CancelledOrder(final Order order) {
        super(
            order.getId(),
            order.getRestaurant(),
            order.getDateTime(),
            order.getShippingRate(),
            order.getClient(),
            order.getFoodRequested()
        );
        switch (order) {
            case DeliveredOrder d -> {
                this.acceptanceTime = Optional.of(d.getAcceptanceTime());
                this.deliveryman = Optional.of(d.getDeliveryman());
                this.deliveryTime = Optional.of(d.getDeliveryTime());
            }
            case AcceptedOrder a -> {
                this.acceptanceTime = Optional.of(a.getAcceptanceTime());
                this.deliveryman = Optional.of(a.getDeliveryman());
                this.deliveryTime = Optional.empty();
            }
            default -> {
                this.acceptanceTime = Optional.empty();
                this.deliveryman = Optional.empty();
                this.deliveryTime = Optional.empty();
            }
        }
    }

    /**
     * @return when the order was accepted if the order were accepted or delivered, empty otherwise
     */
    public Optional<Timestamp> getAcceptanceTime() {
        return acceptanceTime;
    }

    /**
     * @return the User of the deliveryman if the order were accepted or delivered, empty otherwise
     */
    public Optional<User> getDeliveryman() {
        return deliveryman;
    }

    /**
     * @return when the order was delivered if the order were delivered, empty otherwise
     */
    public Optional<Timestamp> getDeliveryTime() {
        return deliveryTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getState() {
        return State.CANCELLED;
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
         * @return AcceptedOrder if everything goes right during the conversion, error otherwise
         */
        public static Result<CancelledOrder> from(
            final Connection connection,
            final Order order
        ) {
            final Result<Order> res = Order.DAO.updateState(
                connection,
                order,
                State.CANCELLED
            );
            if (!res.isSuccess()) {
                // Propagate error
                return Result.failure(res.getErrorMessage());
            }
            return Result.success(new CancelledOrder(order));
        }
    }
}
