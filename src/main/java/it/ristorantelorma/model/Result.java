package it.ristorantelorma.model;

import java.util.Objects;

/**
 * Helper class used as return value for DAO functions.
 * @param <T> the class of the value
 */
public final class Result<T> {
    private final T value;
    private final String errorMessage;
    private final boolean success;

    private Result(final T value, final String errorMessage, final boolean success) {
        this.value = value;
        this.errorMessage = errorMessage;
        this.success = success;
    }

    /**
     * Returns a Result with the specified non-null value.
     * @param <T> the class of the value
     * @param value the value to be present, which must be non-null
     * @return a Result with the value present
     * @throws NullPointerException if value is null
     */
    public static <T> Result<T> success(final T value) {
        Objects.requireNonNull(value);
        return new Result<>(value, null, true);
    }

    /**
     * Returns a Result with the specified non-null error message.
     * @param <T> the class of the non-existant value
     * @param errorMessage the error message to be present, which must be non-null
     * @return a Result with the value present
     * @throws NullPointerException if errorMessage is null
     */
    public static <T> Result<T> failure(final String errorMessage) {
        Objects.requireNonNull(errorMessage);
        return new Result<>(null, errorMessage, false);
    }

    /**
     * If a value is present, returns the value, otherwise throws NoSuchElementException.
     * @return the non-null value
     * @throws NoSuchElementException it the value is not present
     */
    public T getValue() {
        return value;
    }

    /**
     * If an error message is present, returns the error message, otherwise throws NoSuchElementException.
     * @return the non-null error message
     * @throws NoSuchElementException it the error message is not present
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return true if a value is present, empty otherwise
     */
    public boolean isSuccess() {
        return success;
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
        } else if (other instanceof Result<?>) {
            final Result<?> r = (Result<?>) other;
            if (success) {
                return r.getValue().equals(value);
            } else {
                return r.getErrorMessage().equals(errorMessage);
            }
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(value, errorMessage);
    }
}
