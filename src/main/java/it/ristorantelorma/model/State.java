package it.ristorantelorma.model;

/**
 * This enum represent the possible states of an Order.
 */
public enum State {
    /**
     * Order waiting to be prepared.
     */
    WAITING,
    /**
     * Order ready to be delivered.
     */
    READY,
    /**
     * Order accepted for delivery.
     */
    ACCEPTED,
    /**
     * Order delivered.
     */
    DELIVERED,
    /**
     * Order cancelled.
     */
    CANCELLED;
}
