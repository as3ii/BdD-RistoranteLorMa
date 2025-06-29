package it.ristorantelorma.model;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.Objects;
import java.math.BigDecimal;

public final class Order {
    private final int id;
    private final String restaurantName;
    private final Timestamp dateTime;
    private final State state;
    private final BigDecimal shippingRate;
    private final String clientUsername;
    private final Optional<Timestamp> acceptanceTime;
    private final Optional<Timestamp> deliveryTime;
    private final Optional<String> deliverymanUsername;

    public Order(
        final int id, final String restaurantName, final Timestamp dateTime,
        final State state, final BigDecimal shippingRate,
        final String clientUsername, final Optional<Timestamp> acceptanceTime,
        final Optional<Timestamp> deliveryTime,
        final Optional<String> deliverymanUsername
    ) {
        this.id = id;
        this.restaurantName = restaurantName;
        this.dateTime = new Timestamp(dateTime.getTime());
        this.state = state;
        this.shippingRate = shippingRate;
        this.clientUsername = clientUsername;
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
        this.deliverymanUsername = deliverymanUsername;
    }

    public Order(
        final int id, final String restaurantName, final Timestamp dateTime,
        final State state, final BigDecimal shippingRate,
        final String clientUsername, final Timestamp acceptanceTime,
        final String deliverymanUsername
    ) {
        this(id, restaurantName, dateTime, state, shippingRate,
            clientUsername, Optional.of(new Timestamp(acceptanceTime.getTime())),
            Optional.empty(),
            Optional.of(deliverymanUsername)
        );
    }

    public Order(
        final int id, final String restaurantName, final Timestamp dateTime,
        final State state, final BigDecimal shippingRate,
        final String clientUsername
    ) {
        this(id, restaurantName, dateTime, state, shippingRate,
            clientUsername, Optional.empty(), Optional.empty(), Optional.empty()
        );
    }

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

    @Override
    public int hashCode() {
         return Objects.hash(this.id);
    }

    //public String toString() { }

    public static final class DAO { }

    public enum State {
        WAITING,
        READY,
        ACCEPTED,
        DELIVERED,
        CANCELLED;
    }
}
