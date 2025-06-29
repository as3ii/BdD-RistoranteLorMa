package it.ristorantelorma.model;

import java.util.Objects;

public final class FoodType {
    private final String name;
    private final Type type;

    public FoodType(final String name, final Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other instanceof FoodType) {
            final FoodType t = (FoodType) other;
            return t.name.equals(this.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
         return Objects.hash(this.name);
    }

    //public String toString() { }

    public static final class DAO { }

    public enum Type {
        DISH,
        DRINK;
    }
}
