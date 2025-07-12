package it.ristorantelorma.model.order;

import java.util.Locale;

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

    /**
     * @param stateStr
     * @return the right state
     * @throws IllegalArgumentException if stateStr is invalid
     */
    public static State fromString(final String stateStr) {
        switch (stateStr.strip().toLowerCase(Locale.getDefault())) {
            case "attesa", "waiting":
                return WAITING;
            case "pronto", "ready":
                return READY;
            case "accettato", "accepted":
                return ACCEPTED;
            case "consegnato", "delivered":
                return DELIVERED;
            case "annullato", "cancelled":
                return CANCELLED;
            default:
                throw new IllegalArgumentException(
                    "Invalid state value: " + stateStr
                );
        }
    }
}
