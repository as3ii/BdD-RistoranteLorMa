package it.ristorantelorma.model;

import it.ristorantelorma.controller.SimpleLogger;
import it.ristorantelorma.model.user.RestaurantUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represent an entry in the RESTAURANTS table of the database.
 */
public final class Restaurant {

    private final RestaurantUser user;
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
    public Restaurant(
        final RestaurantUser user,
        final String restaurantName,
        final String vatID,
        final Timestamp openingTime,
        final Timestamp closingTime
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
    public RestaurantUser getUser() {
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
            throw new UnsupportedOperationException(
                "Utility class and cannot be instantiated"
            );
        }

        /**
         * @param connection
         * @param result
         * @param optUser can be null
         * @return the Restaurant if there are no errors
         * @throws IllegalStateException
         */
        private static Result<Restaurant> fromRestaurantResultSet(
            final Connection connection,
            final ResultSet result,
            final RestaurantUser optUser
        ) throws SQLException {
            final String username = result.getString("username");
            final String restaurantName = result.getString("nome_attività");

            final RestaurantUser user;
            if (optUser == null) {
                final Result<Optional<RestaurantUser>> tmpUser =
                    RestaurantUser.DAO.find(connection, username);
                if (!tmpUser.isSuccess()) {
                    // Propagate the error
                    return Result.failure(tmpUser.getErrorMessage());
                }
                if (!tmpUser.getValue().isPresent()) {
                    final String errorMessage =
                        "The Restaurant have an invalid username: " + username;
                    LOGGER.log(Level.SEVERE, errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
                user = tmpUser.getValue().get();
            } else {
                user = optUser;
            }

            final String vatID = result.getString("p_iva");
            final Timestamp openingTime = result.getTimestamp("ora_apertura");
            final Timestamp closingTime = result.getTimestamp("ora_chiusura");

            return Result.success(
                new Restaurant(
                    user,
                    restaurantName,
                    vatID,
                    openingTime,
                    closingTime
                )
            );
        }

        /**
         * Find in the database the Restaurant with the given name.
         * @param connection
         * @param restaurantName
         * @return Optional.of(Restaurant) if it exists, Optional.empty() if no Restaurant was found, error otherwise
         * @throws IllegalStateException if the Restaurant searched exists but the linked User no.
         */
        public static Result<Optional<Restaurant>> find(
            final Connection connection,
            final String restaurantName
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.FIND_RESTAURANT_BY_NAME,
                    restaurantName
                );
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final Result<Restaurant> resRestaurant = fromRestaurantResultSet(
                        connection,
                        result,
                        null
                    );
                    if (resRestaurant.isSuccess()) {
                        return Result.success(
                            Optional.of(resRestaurant.getValue())
                        );
                    } else {
                        return Result.failure(resRestaurant.getErrorMessage());
                    }
                } else {
                    return Result.success(Optional.empty());
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed research of Restaurant with restaurantName: "
                    + restaurantName;
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
        public static Result<Optional<Restaurant>> find(
            final Connection connection,
            final RestaurantUser user
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.FIND_RESTAURANT_BY_USERNAME,
                    user.getUsername()
                );
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final Result<Restaurant> resRestaurant = fromRestaurantResultSet(
                        connection,
                        result,
                        user
                    );
                    if (resRestaurant.isSuccess()) {
                        return Result.success(
                            Optional.of(resRestaurant.getValue())
                        );
                    } else {
                        return Result.failure(resRestaurant.getErrorMessage());
                    }
                } else {
                    return Result.success(Optional.empty());
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed research of Restaurant with user: "
                    + user.getUsername();
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
        public static Result<Optional<Restaurant>> findByUsername(
            final Connection connection,
            final String username
        ) {
            final Result<Optional<RestaurantUser>> tmpUser =
                RestaurantUser.DAO.find(connection, username);
            if (!tmpUser.isSuccess()) {
                // Propagate the error
                return Result.failure(tmpUser.getErrorMessage());
            }
            if (tmpUser.getValue().isEmpty()) {
                final String errorMessage =
                    "User not found while searching for Restaurant with username: "
                    + username;
                LOGGER.log(Level.SEVERE, errorMessage);
                return Result.failure(errorMessage);
            }
            final RestaurantUser user = tmpUser.getValue().get();
            return find(connection, user);
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
            final Connection connection,
            final RestaurantUser user,
            final String restaurantName,
            final String vatID,
            final Timestamp openingTime,
            final Timestamp closingTime
        ) {
            final Result<Optional<Restaurant>> restaurant = find(
                connection,
                user
            );

            if (!restaurant.isSuccess()) {
                // Propagate the error
                return Result.failure(restaurant.getErrorMessage());
            }
            if (restaurant.getValue().isPresent()) {
                final String errorMessage =
                    "Restaurant '"
                    + restaurantName
                    + "' not inserted, it already exists";
                LOGGER.log(Level.WARNING, errorMessage);
                return Result.failure(errorMessage);
            }

            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.INSERT_RESTAURANT,
                    user.getUsername(),
                    restaurantName,
                    vatID,
                    openingTime,
                    closingTime
                );
            ) {
                final int rows = statement.executeUpdate();
                if (rows < 1) {
                    final String errorMessage =
                        "Failed restaurant insertion, no rows added";
                    LOGGER.log(Level.SEVERE, errorMessage);
                    return Result.failure(errorMessage);
                } else {
                    return Result.success(
                        new Restaurant(
                            user,
                            restaurantName,
                            vatID,
                            openingTime,
                            closingTime
                        )
                    );
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed insertion of restaurant: " + restaurantName + " - " + e.getMessage();
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Lists all restaurants in the database.
         * @param connection
         * @return a Collection<Restaurant> if there are no errors
         * @throws IllegalStateException if one Restaurant have a non-existent linked User.
         */
        public static Result<Collection<Restaurant>> list(
            final Connection connection
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.LIST_RESTAURANTS
                );
                ResultSet result = statement.executeQuery();
            ) {
                final Collection<Restaurant> restaurants = new HashSet<>();
                while (result.next()) {
                    final Result<Restaurant> resRestaurant = fromRestaurantResultSet(
                        connection,
                        result,
                        null
                    );
                    if (resRestaurant.isSuccess()) {
                        restaurants.add(resRestaurant.getValue());
                    } else {
                        return Result.failure(resRestaurant.getErrorMessage());
                    }
                }
                return Result.success(restaurants);
            } catch (SQLException e) {
                final String errorMessage = "Failed listing restaurants";
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Find the Restaurant with the most orders.
         * @param connection
         * @return a pair <Restaurant, Integer> if there are no errors
         */
        public static Result<Entry<Restaurant, Integer>> getTopByOrderCount(
            final Connection connection
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.FIND_RESTAURANT_MOST_ORDERS
                );
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final Result<Restaurant> resRestaurant = fromRestaurantResultSet(
                        connection,
                        result,
                        null
                    );
                    if (resRestaurant.isSuccess()) {
                        final int count = result.getInt("numero_ordini");
                        return Result.success(
                            new SimpleImmutableEntry<>(
                                resRestaurant.getValue(),
                                count
                            )
                        );
                    } else {
                        return Result.failure(resRestaurant.getErrorMessage());
                    }
                } else {
                    final String errorMessage =
                        "No value returned from the query";
                    LOGGER.log(Level.WARNING, errorMessage);
                    return Result.failure(errorMessage);
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed getting restaurant with the most orders";
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Find the Restaurant with the most negative reviews (mean).
         * @param connection
         * @return a pair <Restaurant, Integer> if there are no errors
         */
        public static Result<Entry<Restaurant, Float>> getTopByNegativeReviews(
            final Connection connection
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.FIND_RESTAURANT_MOST_NEGATIVE_REVIEWS
                );
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final Result<Restaurant> resRestaurant = fromRestaurantResultSet(
                        connection,
                        result,
                        null
                    );
                    if (resRestaurant.isSuccess()) {
                        final float average = result.getFloat("average");
                        return Result.success(
                            new SimpleImmutableEntry<>(
                                resRestaurant.getValue(),
                                average
                            )
                        );
                    } else {
                        return Result.failure(resRestaurant.getErrorMessage());
                    }
                } else {
                    final String errorMessage =
                        "No value returned from the query";
                    LOGGER.log(Level.WARNING, errorMessage);
                    return Result.failure(errorMessage);
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed getting restaurant with the most negative reviews";
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }
    }
}
