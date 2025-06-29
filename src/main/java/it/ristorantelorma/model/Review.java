package it.ristorantelorma.model;

import java.util.Optional;
import java.util.Objects;
import java.sql.Timestamp;

public final class Review {
    private final int id;
    private final String restaurantName;
    private final Timestamp date;
    private final Vote vote;
    private final Optional<String> comment;
    private final String username;

    public Review(
        final int id, final String restaurantName, final Timestamp date,
        final Vote vote, final Optional<String> comment, final String username
    ) {
        this.id = id;
        this.restaurantName = restaurantName;
        this.date = new Timestamp(date.getTime());
        this.vote = vote;
        this.comment = comment;
        this.username = username;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other instanceof Review) {
            final Review r = (Review) other;
            return r.id == this.id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
         return Objects.hash(this.id);
    }

    //public String toString() { }

    public static final class DAO { }

    public enum Vote {
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5);

        private final int value;

        Vote(final int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}
