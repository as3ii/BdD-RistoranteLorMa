package it.ristorantelorma.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represent an entry in the RESTAURANTS table of the database.
 */
public final class Restaurant {
    private final User user;
    private final String restaurantName;
    private final String vatID;
    private final Timestamp openingTime;
    private final Timestamp closingTime;

    /**
     * @param user              the User that manage the Restaurant
     * @param restaurantName    the name of the Restaurant
     * @param vatID             the VAT ID of the restaurant
     * @param openingTime       when the restaurant opens
     * @param closingTime       when the restaurant closes
     */
    public Restaurant(final User user, final String restaurantName,
        final String vatID, final Timestamp openingTime, final Timestamp closingTime
    ) {
        this.user = user;
        this.restaurantName = restaurantName;
        this.vatID = vatID;
        this.openingTime = new Timestamp(openingTime.getTime());
        this.closingTime = new Timestamp(closingTime.getTime());
    }

    /**
     * @return the User that manage the Restaurant
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the Restaurant name
     */
    public String getRestaurantName() {
        return restaurantName;
    }

    /**
     * @return the VAT ID of the Restaurant
     */
    public String getVatNumber() {
        return vatID;
    }

    /**
     * @return when the Restaurant opens
     */
    public Timestamp getOpeningTime() {
        return new Timestamp(openingTime.getTime());
    }

    /**
     * @return when the Restaurant closes
     */
    public Timestamp getClosingTime() {
        return new Timestamp(closingTime.getTime());
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
        } else if (other instanceof Restaurant) {
            final Restaurant r = (Restaurant) other;
            return r.restaurantName.equals(this.restaurantName);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
         return Objects.hash(this.restaurantName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "Restaurant = { user = \"%s\", restaurantName = \"%s\", vatID = \"%s\", openingTime = %s, closingTime = %s }",
            user.getUsername(),
            restaurantName,
            vatID,
            openingTime,
            closingTime
        );
    }

    /**
     * Inner class that handles requests to the database.
     */
    public static final class DAO { }
}
