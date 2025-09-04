package it.ristorantelorma.model;

import java.util.Locale;

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
     * @param typeStr
     * @return the right Vote
     * @throws IllegalArgumentException if typeStr is invalid
     */
    public static Vote fromString(final String typeStr) {
        switch (typeStr.strip().toLowerCase(Locale.getDefault())) {
            case "one", "1":
                return ONE;
            case "two", "2":
                return TWO;
            case "three", "3":
                return THREE;
            case "four", "4":
                return FOUR;
            case "five", "5":
                return FIVE;
            default:
                throw new IllegalArgumentException(
                    "Invalid MacroType value: " + typeStr
                );
        }
    }
    /**
     * @return integer equivalent of the Vote
     */
    public int getValue() {
        return this.value;
    }
}
