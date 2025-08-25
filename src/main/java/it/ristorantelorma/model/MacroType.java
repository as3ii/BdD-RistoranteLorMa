package it.ristorantelorma.model;

import java.util.Locale;

/**
 * This enum represent the possible macro-types.
 */
public enum MacroType {
    /**
     * The Food is a dish.
     */
    DISH,
    /**
     * The Food is a drink.
     */
    DRINK;

    /**
     * @param typeStr
     * @return the right MacroType
     * @throws IllegalArgumentException if typeStr is invalid
     */
    public static MacroType fromString(final String typeStr) {
        switch (typeStr.strip().toLowerCase(Locale.getDefault())) {
            case "cibo", "dish":
                return DISH;
            case "bevanda", "drink":
                return DRINK;
            default:
                throw new IllegalArgumentException(
                    "Invalid MacroType value: " + typeStr
                );
        }
    }

    /**
     * @param type
     * @return string equivalent to be used in SQL queries
     * @throws IllegalArgumentException if type is invalid
     */
    public static String toSQLStr(final MacroType type) {
        switch (type) {
            case DISH:
                return "cibo";
            case DRINK:
                return "bevanda";
            default:
                throw new IllegalArgumentException(
                    "Invalid type value: " + type.toString()
                );
        }
    }

    /**
     * @return string equivalent to be used in SQL queries
     * @throws IllegalArgumentException if type is invalid
     */
    public String toSQLStr() {
        return toSQLStr(this);
    }
}
