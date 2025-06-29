package it.ristorantelorma.model;

import java.sql.Timestamp;
import java.util.Objects;

public final class Restaurant {
    private final String username;
    private final String restaurantName;
    private final String vat;
    private final Timestamp openingTime;
    private final Timestamp closingTime;

    public Restaurant(final String username, final String restaurantName,
        final String vat, final Timestamp openingTime, final Timestamp closingTime
    ) {
        this.username = username;
        this.restaurantName = restaurantName;
        this.vat = vat;
        this.openingTime = new Timestamp(openingTime.getTime());
        this.closingTime = new Timestamp(closingTime.getTime());
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other instanceof Restaurant) {
            final Restaurant r = (Restaurant) other;
            return r.restaurantName.equals(this.restaurantName);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
         return Objects.hash(this.restaurantName);
    }

    //public String toString() { }

    public static final class DAO { }
}
