package it.ristorantelorma.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Food {
    private final int id;
    private final String name;
    private final String restaurantName;
    private final BigDecimal price;
    private final String type;

    public Food(final int id, final String name, final String restaurantName, final BigDecimal price, final String type) {
        this.id = id;
        this.name = name;
        this.restaurantName = restaurantName;
        this.price = price;
        this.type = type;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    //public String toString() { }

    public static final class DAO { }
}
