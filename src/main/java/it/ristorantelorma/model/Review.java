package it.ristorantelorma.model;

import java.util.Optional;
import java.util.Objects;
import java.sql.Timestamp;

/**
 * Represent an entry in the REVIEWS table of the database.
 */
public final class Review {
    private final int id;
    private final Restaurant restaurant;
    private final Timestamp date;
    private final Vote vote;
    private final Optional<String> comment;
    private final User user;

    /**
     * @param id                the ID of the record in the database
     * @param restaurant        the reviewed Restaurant
     * @param date              when the Review was written
     * @param vote              the Vote given by the User
     * @param comment           Optional comment
     * @param user              the User that wrote the review
     */
    public Review(
        final int id, final Restaurant restaurant, final Timestamp date,
        final Vote vote, final Optional<String> comment, final User user
    ) {
        this.id = id;
        this.restaurant = restaurant;
        this.date = new Timestamp(date.getTime());
        this.vote = vote;
        this.comment = comment;
        this.user = user;
    }

    /**
     * @return the ID of the record in the database
     */
    public int getId() {
        return id;
    }

    /**
     * @return the name of the reviewed Restaurant
     */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     * @return when the Review was written
     */
    public Timestamp getDate() {
        return new Timestamp(date.getTime());
    }

    /**
     * @return the Vote given by the User
     */
    public Vote getVote() {
        return vote;
    }

    /**
     * @return the comment given by the User, empty otherwise
     */
    public Optional<String> getComment() {
        return comment;
    }

    private String getCommentString() {
        if (comment.isPresent()) {
            return comment.get();
        } else {
            return "None";
        }
    }

    /**
     * @return the username of the User that wrote the Review
     */
    public User getUser() {
        return user;
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
        } else if (other instanceof Review) {
            final Review r = (Review) other;
            return r.id == this.id;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
         return Objects.hash(this.id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "Review = { id = %s, user = \"%s\", restaurant = \"%s\", date = %s, vote = %s, comment = \"%s\" }",
            id,
            user.getUsername(),
            restaurant.getRestaurantName(),
            date,
            vote.getValue(),
            getCommentString()
        );
    }

    /**
     * Inner class that handles requests to the database.
     */
    public static final class DAO { }
}
