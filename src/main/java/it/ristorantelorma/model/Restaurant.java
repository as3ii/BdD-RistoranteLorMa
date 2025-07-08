package it.ristorantelorma.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.ristorantelorma.controller.SimpleLogger;

/**
 * Represent an entry in the RESTAURANTS table of the database.
 */
public final class Restaurant {
    private final User user;
    private final String restaurantName;
    private final String vatID;
    private final Timestamp openingTime;
    private final Timestamp closingTime;

    /**
     * @param user              the User that manage the Restaurant
     * @param restaurantName    the name of the Restaurant
     * @param vatID             the VAT ID of the restaurant
     * @param openingTime       when the restaurant opens
     * @param closingTime       when the restaurant closes
     */
    public Restaurant(final User user, final String restaurantName,
        final String vatID, final Timestamp openingTime, final Timestamp closingTime
    ) {
        this.user = user;
        this.restaurantName = restaurantName;
        this.vatID = vatID;
        this.openingTime = new Timestamp(openingTime.getTime());
        this.closingTime = new Timestamp(closingTime.getTime());
    }

    /**
     * @return the User that manage the Restaurant
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the Restaurant name
     */
    public String getRestaurantName() {
        return restaurantName;
    }

    /**
     * @return the VAT ID of the Restaurant
     */
    public String getVatNumber() {
        return vatID;
    }

    /**
     * @return when the Restaurant opens
     */
    public Timestamp getOpeningTime() {
        return new Timestamp(openingTime.getTime());
    }

    /**
     * @return when the Restaurant closes
     */
    public Timestamp getClosingTime() {
        return new Timestamp(closingTime.getTime());
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
        } else if (other instanceof Restaurant) {
            final Restaurant r = (Restaurant) other;
            return r.restaurantName.equals(this.restaurantName);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
         return Objects.hash(this.restaurantName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "Restaurant = { user = \"%s\", restaurantName = \"%s\", vatID = \"%s\", openingTime = %s, closingTime = %s }",
            user.getUsername(),
            restaurantName,
            vatID,
            openingTime,
            closingTime
        );
    }

    /**
     * Inner class that handles requests to the database.
     */
    public static final class DAO {
        private static final String CLASS_NAME = DAO.class.getName();
        private static final Logger LOGGER = SimpleLogger.getLogger(CLASS_NAME);

        private DAO() {
            throw new UnsupportedOperationException("Utility class and cannot be instantiated");
        }

        /**
         * Find in the database the Restaurant with the given name.
         * @param connection
         * @param restaurantName
         * @return Optional.of(Restaurant) if it exists, Optional.empty() if no Restaurant was found, error otherwise
         * @throws IllegalStateException if the Restaurant searched exists but the linked User no.
         */
        public static Result<Optional<Restaurant>> findByRestaurantName(
            final Connection connection, final String restaurantName
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(connection, Queries.FIND_RESTAURANT_BY_NAME, restaurantName);
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final String username = result.getString("username");
                    final Result<Optional<User>> tmpUser = User.DAO.find(connection, username);
                    if (!tmpUser.isSuccess()) {
                        // Propagate the error
                        return Result.failure(tmpUser.getErrorMessage());
                    }
                    if (!tmpUser.getValue().isPresent()) {
                        final String errorMessage = "The Restaurant have an invalid username: " + username;
                        LOGGER.log(Level.SEVERE, errorMessage);
                        throw new IllegalStateException(errorMessage);
                    }

                    final User user = tmpUser.getValue().get();
                    final String vatID = result.getString("p_iva");
                    final Timestamp openingTime = result.getTimestamp("ora_apertura");
                    final Timestamp closingTime = result.getTimestamp("ora_chiusura");
                    return Result.success(Optional.of(
                        new Restaurant(user, restaurantName, vatID, openingTime, closingTime)
                    ));
                } else {
                    return Result.success(Optional.empty());
                }
            } catch (SQLException e) {
                final String errorMessage = "Failed research of Restaurant with restaurantName: " + restaurantName;
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Find in the database the Restaurant owned by the given User.
         * @param connection
         * @param user
         * @return Optional.of(Restaurant) if it exists, Optional.empty() if no Restaurant was found, error otherwise
         */
        public static Result<Optional<Restaurant>> findByUser(final Connection connection, final User user) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection, Queries.FIND_RESTAURANT_BY_USERNAME, user.getUsername()
                );
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final String restaurantName = result.getString("nome_attività");
                    final String vatID = result.getString("p_iva");
                    final Timestamp openingTime = result.getTimestamp("ora_apertura");
                    final Timestamp closingTime = result.getTimestamp("ora_chiusura");
                    return Result.success(Optional.of(
                        new Restaurant(user, restaurantName, vatID, openingTime, closingTime)
                    ));
                } else {
                    return Result.success(Optional.empty());
                }
            } catch (SQLException e) {
                final String errorMessage = "Failed research of Restaurant with user: " + user.getUsername();
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Find in the database the Restaurant owned by the given username.
         * @param connection
         * @param username
         * @return Optional.of(Restaurant) if it exists, Optional.empty() if no Restaurant was found, error otherwise
         */
        public static Result<Optional<Restaurant>> findByUsername(final Connection connection, final String username) {
            final Result<Optional<User>> tmpUser = User.DAO.find(connection, username);
            if (!tmpUser.isSuccess()) {
                // Propagate the error
                return Result.failure(tmpUser.getErrorMessage());
            }
            if (tmpUser.getValue().isEmpty()) {
                final String errorMessage = "User not found while searching for Restaurant with username: " + username;
                LOGGER.log(Level.SEVERE, errorMessage);
                return Result.failure(errorMessage);
            }
            final User user = tmpUser.getValue().get();
            return findByUser(connection, user);
        }

        /**
         * Insert a new Restaurant in the database.
         * @param connection
         * @param user
         * @param restaurantName
         * @param vatID
         * @param openingTime
         * @param closingTime
         * @return the Restaurant if it has been correctly added, empty otherwise
         */
        public static Result<Restaurant> insert(
            final Connection connection, final User user, final String restaurantName,
            final String vatID, final Timestamp openingTime, final Timestamp closingTime
        ) {
            final Result<Optional<Restaurant>> restaurant = findByUser(connection, user);

            if (!restaurant.isSuccess()) {
                // Propagate the error
                return Result.failure(restaurant.getErrorMessage());
            }
            if (restaurant.getValue().isPresent()) {
                final String errorMessage = "Restaurant '" + restaurantName + "' not inserted, it already exists";
                LOGGER.log(Level.WARNING, errorMessage);
                return Result.failure(errorMessage);
            }

            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection, Queries.INSERT_RESTAURANT,
                    user.getUsername(), restaurantName, vatID, openingTime, closingTime
                );
            ) {
                final int rows = statement.executeUpdate();
                if (rows < 1) {
                    final String errorMessage = "Failed restaurant insertion, no rows added";
                    LOGGER.log(Level.SEVERE, errorMessage);
                    return Result.failure(errorMessage);
                } else {
                    return Result.success(
                        new Restaurant(user, restaurantName, vatID, openingTime, closingTime)
                    );
                }
            } catch (SQLException e) {
                final String errorMessage = "Failed insertion of restaurant: " + restaurantName;
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Lists all restaurants in the database.
         * @param connection
         * @return a Set<Restaurant> if there are no error
         * @throws IllegalStateException if one Restaurant have a non-existent linked User.
         */
        public static Result<Set<Restaurant>> listRestaurants(final Connection connection) {
            try (
                PreparedStatement statement = DBHelper.prepare(connection, Queries.LIST_RESTAURANTS);
                ResultSet result = statement.executeQuery();
            ) {
                final Set<Restaurant> restaurants = new HashSet<>();
                while (result.next()) {
                    final String restaurantName = result.getString("nome_attività");
                    final String username = result.getString("username");
                    final Result<Optional<User>> tmpUser = User.DAO.find(connection, username);
                    if (!tmpUser.isSuccess()) {
                        // Propagate error
                        return Result.failure(tmpUser.getErrorMessage());
                    }
                    if (!tmpUser.getValue().isPresent()) {
                        final String errorMessage =
                            "The Restaurant '" + restaurantName
                            + "' have an invalid username: " + username;
                        LOGGER.log(Level.SEVERE, errorMessage);
                        throw new IllegalStateException(errorMessage);
                    }

                    final User user = tmpUser.getValue().get();
                    final String vatID = result.getString("p_iva");
                    final Timestamp openingTime = result.getTimestamp("ora_apertura");
                    final Timestamp closingTime = result.getTimestamp("ora_chiusura");
                    restaurants.add(new Restaurant(
                        user, restaurantName, vatID, openingTime, closingTime
                    ));
                }
                return Result.success(restaurants);
            } catch (SQLException e) {
                final String errorMessage = "Failed listing restaurants";
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }
    }
}
