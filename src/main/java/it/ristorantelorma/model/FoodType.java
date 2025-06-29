package it.ristorantelorma.model;

import java.util.Objects;

/**
 * Represent an entry in the FOOD_TYPES table of the database.
 */
public final class FoodType {
    private final String name;
    private final MacroType type;

    /**
     * @param name the name of the type
     * @param type @see MacroType
     */
    public FoodType(final String name, final MacroType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @return Food type name
     */
    public String getName() {
        return name;
    }

    /**
     * @return @see MacroType
     */
    public MacroType getType() {
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
        } else if (other instanceof FoodType) {
            final FoodType t = (FoodType) other;
            return t.name.equals(this.name);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
         return Objects.hash(this.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "FoodType = { name = \"%s\", type = \"%s\" }",
            name,
            type
        );
    }

    /**
     * Inner class that handles requests to the database.
     */
    public static final class DAO { }
}
