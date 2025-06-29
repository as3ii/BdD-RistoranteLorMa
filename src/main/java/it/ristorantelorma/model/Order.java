package it.ristorantelorma.model;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.math.BigDecimal;

/**
 * Represent an entry in the ORDERS table of the database.
 */
public final class Order {
    private final int id;
    private final Restaurant restaurant;
    private final Timestamp dateTime;
    private final State state;
    private final BigDecimal shippingRate;
    private final User client;
    private final Map<Food, Integer> foodRequested;
    private final Optional<Timestamp> acceptanceTime;
    private final Optional<Timestamp> deliveryTime;
    private final Optional<User> deliveryman;

    /**
     * @param id                the ID of the record in the database
     * @param restaurant        the Restaurant to which the order was placed
     * @param dateTime          when the order was created
     * @param state             the State of the order
     * @param shippingRate      the price for the shipment
     * @param client            the client that placed the order
     * @param foodRequested     the food requested by the client
     * @param acceptanceTime
     * @param deliveryTime
     * @param deliveryman
     */
    public Order(
        final int id, final Restaurant restaurant, final Timestamp dateTime,
        final State state, final BigDecimal shippingRate,
        final User client, final Map<Food, Integer> foodRequested,
        final Optional<Timestamp> acceptanceTime,
        final Optional<Timestamp> deliveryTime,
        final Optional<User> deliveryman
    ) {
        this.id = id;
        this.restaurant = restaurant;
        this.dateTime = new Timestamp(dateTime.getTime());
        this.state = state;
        this.shippingRate = shippingRate;
        this.client = client;
        this.foodRequested = new HashMap<>(foodRequested);
        if (acceptanceTime.isPresent()) {
            this.acceptanceTime = Optional.of(new Timestamp(acceptanceTime.get().getTime()));
        } else {
            this.acceptanceTime = Optional.empty();
        }
        if (deliveryTime.isPresent()) {
            this.deliveryTime = Optional.of(new Timestamp(deliveryTime.get().getTime()));
        } else {
            this.deliveryTime = Optional.empty();
        }
        this.deliveryman = deliveryman;
    }

    /**
     * @param id                the ID of the record in the database
     * @param restaurant        the Restaurant to which the order was placed
     * @param dateTime          when the order was created
     * @param state             the State of the order
     * @param shippingRate      the price for the shipment
     * @param client            the client that placed the order
     * @param foodRequested     the food requested by the client
     * @param acceptanceTime
     * @param deliveryman
     */
    public Order(
        final int id, final Restaurant restaurant, final Timestamp dateTime,
        final State state, final BigDecimal shippingRate,
        final User client, final Map<Food, Integer> foodRequested,
        final Timestamp acceptanceTime, final User deliveryman
    ) {
        this(id, restaurant, dateTime, state, shippingRate,
            client, foodRequested,
            Optional.of(new Timestamp(acceptanceTime.getTime())),
            Optional.empty(),
            Optional.of(deliveryman)
        );
    }

    /**
     * @param id                the ID of the record in the database
     * @param restaurant        the Restaurant to which the order was placed
     * @param dateTime          when the order was created
     * @param state             the State of the order
     * @param shippingRate      the price for the shipment
     * @param client            the client that placed the order
     * @param foodRequested     the food requested by the client
     */
    public Order(
        final int id, final Restaurant restaurant, final Timestamp dateTime,
        final State state, final BigDecimal shippingRate,
        final User client, final Map<Food, Integer> foodRequested
    ) {
        this(id, restaurant, dateTime, state, shippingRate, client,
            foodRequested, Optional.empty(), Optional.empty(), Optional.empty()
        );
    }

    /**
     * @return ID of the record in the database
     */
    public int getId() {
        return id;
    }

    /**
     * @return Restaurant linked to the order
     */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     * @return when the order was created
     */
    public Timestamp getDateTime() {
        return new Timestamp(dateTime.getTime());
    }

    /**
     * @return the State of the order
     */
    public State getState() {
        return state;
    }

    /**
     * @return the price for the shipment
     */
    public BigDecimal getShippingRate() {
        return shippingRate;
    }

    /**
     * @return the User of the client
     */
    public User getClient() {
        return client;
    }

    /**
     * @return the food requested by the client
     */
    public Map<Food, Integer> getFoodRequested() {
        return Map.copyOf(foodRequested);
    }

    private String getFoodRequestedString() {
        return foodRequested.entrySet()
            .stream()
            .map(e -> "\"" + e.getKey() + "\" = " + e.getValue())
            .collect(Collectors.joining(", ", "{ ", " }"));
    }

    /**
     * @return when the order was accepted if the state is State.ACCEPTED or State.DELIVERED, empty otherwise
     */
    public Optional<Timestamp> getAcceptanceTime() {
        if (acceptanceTime.isPresent()) {
            return Optional.of(new Timestamp(acceptanceTime.get().getTime()));
        } else {
            return Optional.empty();
        }
    }

    private String getAcceptanceTimeString() {
        if (acceptanceTime.isPresent()) {
            return this.acceptanceTime.get().toString();
        } else {
            return "None";
        }
    }

    /**
     * @return when the order was delivered if the state is State.DELIVERED, empty otherwise
     */
    public Optional<Timestamp> getDeliveryTime() {
        if (deliveryTime.isPresent()) {
            return Optional.of(new Timestamp(deliveryTime.get().getTime()));
        } else {
            return Optional.empty();
        }
    }

    private String getDeliveryTimeString() {
        if (deliveryTime.isPresent()) {
            return this.deliveryTime.get().toString();
        } else {
            return "None";
        }
    }

    /**
     * @return the User of the deliveryman if the state is State.ACCEPTED or State.DELIVERED, empty otherwise
     */
    public Optional<User> getDeliveryman() {
        return deliveryman;
    }

    private String getDeliverymanString() {
        if (deliveryman.isPresent()) {
            return this.deliveryman.get().getUsername();
        } else {
            return "None";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other instanceof Order) {
            final Order o = (Order) other;
            return o.id == this.id;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
         return Objects.hash(this.id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "Order = { id = %id, client = \"%s\", restaurant = \"%s\", "
            + "dateTime = %s, shippingRate = %s, state = \"%s\", acceptanceTime = %s, "
            + "deliveryman = \"%s\", deliveryTime = %s, foodRequested = %s }",
            id,
            client.getUsername(),
            restaurant.getRestaurantName(),
            dateTime,
            shippingRate,
            state,
            getAcceptanceTimeString(),
            getDeliverymanString(),
            getDeliveryTimeString(),
            getFoodRequestedString()
        );
    }

    /**
     * Inner class that handles requests to the database.
     */
    public static final class DAO { }
}
