package it.ristorantelorma.model;

/**
 * This enum represent the possible votes in a Review.
 */
public enum Vote {
    /**
     * One (the lower).
     */
    ONE(1),
    /**
     * Two.
     */
    TWO(2),
    /**
     * Three.
     */
    THREE(3),
    /**
     * Four.
     */
    FOUR(4),
    /**
     * Five (higher).
     */
    FIVE(5);

    private final int value;

    Vote(final int value) {
        this.value = value;
    }

    /**
     * @return integer equivalent of the Vote
     */
    public int getValue() {
        return this.value;
    }
}
