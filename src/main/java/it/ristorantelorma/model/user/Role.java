package it.ristorantelorma.model.user;

import java.util.Locale;

/**
 * This enum represent the possible roles of an User.
 */
public enum Role {
    /**
     * Administrator.
     */
    ADMIN,
    /**
     * The User manage a Restaurant.
     */
    RESTAURANT,
    /**
     * The User is a client.
     */
    CLIENT,
    /**
     * The User is a delivery man.
     */
    DELIVERYMAN;

    /**
     * @param roleStr
     * @return the right role
     * @throws IllegalArgumentException if roleStr is invalid
     */
    public static Role fromString(final String roleStr) {
        switch (roleStr.strip().toLowerCase(Locale.getDefault())) {
            case "amministratore", "admin":
                return ADMIN;
            case "ristorante", "restaurant":
                return RESTAURANT;
            case "cliente", "client":
                return CLIENT;
            case "fattorino", "deliveryman":
                return DELIVERYMAN;
            default:
                throw new IllegalArgumentException(
                    "Invalid role value: " + roleStr
                );
        }
    }

    /**
     * @param role
     * @return string equivalent to be used in SQL queries
     * @throws IllegalArgumentException if role is invalid
     */
    public static String toSQLStr(final Role role) {
        switch (role) {
            case ADMIN:
                return "admin";
            case RESTAURANT:
                return "ristorante";
            case CLIENT:
                return "cliente";
            case DELIVERYMAN:
                return "fattorino";
            default:
                throw new IllegalArgumentException(
                    "Invalid role value: " + role.toString()
                );
        }
    }

    /**
     * @return string equivalent to be used in SQL queries
     * @throws IllegalArgumentException if role is invalid
     */
    public String toSQLStr() {
        return toSQLStr(this);
    }
}
