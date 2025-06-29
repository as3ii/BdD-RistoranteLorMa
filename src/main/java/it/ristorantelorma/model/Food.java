package it.ristorantelorma.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represent an entry in the FOODS table of the database.
 */
public final class Food {
    private final int id;
    private final String name;
    private final Restaurant restaurant;
    private final BigDecimal price;
    private final FoodType type;

    /**
     * @param id                the ID of the record in the database
     * @param name              the name of the food
     * @param restaurant        @see Restaurant
     * @param price
     * @param type              @see FoodType
     */
    public Food(final int id, final String name, final Restaurant restaurant, final BigDecimal price, final FoodType type) {
        this.id = id;
        this.name = name;
        this.restaurant = restaurant;
        this.price = price;
        this.type = type;
    }

    /**
    * @return ID of the record in the database
    */
    public int getId() {
        return id;
    }

    /**
     * @return Food name
     */
    public String getName() {
        return name;
    }

    /**
     * @return @see Restaurant
     */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     * @return price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * @return @see FoodType
     */
    public FoodType getType() {
        return type;
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
        } else if (other instanceof Food) {
            final Food f = (Food) other;
            return f.id == this.id;
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
            "Food = { id = %s, name = \"%s\", restaurant = \"%s\", price = %s, type = \"%s\" }",
            id,
            name,
            restaurant.getRestaurantName(),
            price,
            type.getName()
        );
    }

    /**
     * Inner class that handles requests to the database.
     */
    public static final class DAO { }
}
