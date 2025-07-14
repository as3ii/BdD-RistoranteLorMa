package it.ristorantelorma.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.ristorantelorma.controller.SimpleLogger;
import it.ristorantelorma.model.user.ClientUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represent an entry in the REVIEWS table of the database.
 */
public final class Review {

    private final int id;
    private final Restaurant restaurant;
    private final Timestamp date;
    private final Vote vote;
    private final Optional<String> comment;
    private final ClientUser user;

    /**
     * @param id                the ID of the record in the database
     * @param restaurant        the reviewed Restaurant
     * @param date              when the Review was written
     * @param vote              the Vote given by the User
     * @param comment           Optional comment
     * @param user              the User that wrote the review
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "The client's credit can be mutated without issues"
    )
    Review(
        final int id,
        final Restaurant restaurant,
        final Timestamp date,
        final Vote vote,
        final Optional<String> comment,
        final ClientUser user
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
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "The client's credit can be mutated without issues"
    )
    public ClientUser getUser() {
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
    public static final class DAO {

        private static final String CLASS_NAME = DAO.class.getName();
        private static final Logger LOGGER = SimpleLogger.getLogger(CLASS_NAME);

        private DAO() {
            throw new UnsupportedOperationException(
                "Utility class and cannot be instantiated"
            );
        }

        /**
         * Lists all reviews in the database of a particular restaurant.
         * @param connection
         * @param restaurant
         * @return Collection<Review> if there are no errors
         * @throws IllegalStateException if one Review is linked to a non-existent User.
         */
        public static Result<Collection<Review>> list(
            final Connection connection,
            final Restaurant restaurant
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.LIST_REVIEWS_OF_RESTAURANT,
                    restaurant.getRestaurantName()
                );
                ResultSet result = statement.executeQuery();
            ) {
                final Collection<Review> reviews = new HashSet<>();
                while (result.next()) {
                    final int id = result.getInt("codice");
                    final Timestamp date = result.getTimestamp("data");
                    final Vote vote = Vote.valueOf(result.getString("voto"));
                    final Optional<String> comment = Optional.ofNullable(
                        result.getString("commento")
                    );
                    final String username = result.getString("username");
                    final Result<Optional<ClientUser>> tmpUser =
                        ClientUser.DAO.find(connection, username);
                    if (!tmpUser.isSuccess()) {
                        // Propagate error
                        return Result.failure(tmpUser.getErrorMessage());
                    }
                    if (!tmpUser.getValue().isPresent()) {
                        final String errorMessage =
                            "The Review (ID: "
                            + id
                            + ") have an invalid username: "
                            + username;
                        LOGGER.log(Level.SEVERE, errorMessage);
                        throw new IllegalStateException(errorMessage);
                    }
                    final ClientUser user = tmpUser.getValue().get();
                    reviews.add(
                        new Review(id, restaurant, date, vote, comment, user)
                    );
                }
                return Result.success(reviews);
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed listing reviews for the restaurant: "
                    + restaurant.getRestaurantName();
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Insert a new Review in the database.
         * @param connection
         * @param restaurant
         * @param date
         * @param vote
         * @param comment
         * @param user
         * @return the Review if it hase been correctly added, error otherwise
         * @throws IllegalStateException if the retrival of the record ID fails
         */
        public static Result<Review> insert(
            final Connection connection,
            final Restaurant restaurant,
            final Timestamp date,
            final Vote vote,
            final Optional<String> comment,
            final ClientUser user
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.INSERT_REVIEW,
                    restaurant.getRestaurantName(),
                    date,
                    String.valueOf(vote.getValue()),
                    comment.orElse(null),
                    user.getUsername()
                );
            ) {
                final int rows = statement.executeUpdate();
                if (rows < 1) {
                    final String errorMessage =
                        "Failed Review insertion, no rows added";
                    LOGGER.log(Level.SEVERE, errorMessage);
                    return Result.failure(errorMessage);
                } else {
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            final int id = keys.getInt("codice");
                            return Result.success(
                                new Review(
                                    id,
                                    restaurant,
                                    date,
                                    vote,
                                    comment,
                                    user
                                )
                            );
                        } else {
                            final String errorMessage =
                                "Insertion of Review seams complete but the retrival of the record ID failed";
                            LOGGER.log(Level.SEVERE, errorMessage);
                            throw new IllegalStateException(errorMessage);
                        }
                    }
                }
            } catch (SQLException e) {
                final String errorMessage = "Failed insertion of the review";
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Delete the given review from the database.
         * @param connection
         * @param review
         * @return success if has been deleted correctly, error otherwise
         */
        public static Result<?> delete(
            final Connection connection,
            final Review review
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.DELETE_REVIEW,
                    review.getId()
                );
            ) {
                final int rows = statement.executeUpdate();
                if (rows < 1) {
                    final String errorMessage =
                        "Failed Review deletion, no rows removed";
                    LOGGER.log(Level.SEVERE, errorMessage);
                    return Result.failure(errorMessage);
                } else {
                    return Result.success(new Object()); // Return dummy value
                }
            } catch (SQLException e) {
                final String errorMessage = "Failed deletion of the review";
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }
    }
}
