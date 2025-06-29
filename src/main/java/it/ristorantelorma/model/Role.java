package it.ristorantelorma.model;

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
}
