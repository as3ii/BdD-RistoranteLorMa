package it.ristorantelorma.model;

import it.ristorantelorma.controller.SimpleLogger;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represent an entry in the FOODS table of the database.
 */
public final class Food {

    private final int id;
    private final String name;
    private final Restaurant restaurant;
    private final BigDecimal price;
    private final FoodType type;

    /**
     * @param id                the ID of the record in the database
     * @param name              the name of the food
     * @param restaurant        @see Restaurant
     * @param price
     * @param type              @see FoodType
     */
    public Food(
        final int id,
        final String name,
        final Restaurant restaurant,
        final BigDecimal price,
        final FoodType type
    ) {
        this.id = id;
        this.name = name;
        this.restaurant = restaurant;
        this.price = price;
        this.type = type;
    }

    /**
     * @return ID of the record in the database
     */
    public int getId() {
        return id;
    }

    /**
     * @return Food name
     */
    public String getName() {
        return name;
    }

    /**
     * @return @see Restaurant
     */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     * @return price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * @return @see FoodType
     */
    public FoodType getType() {
        return type;
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
        } else if (other instanceof Food) {
            final Food f = (Food) other;
            return f.id == this.id;
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
            "Food = { id = %s, name = \"%s\", restaurant = \"%s\", price = %s, type = \"%s\" }",
            id,
            name,
            restaurant.getRestaurantName(),
            price,
            type.getName()
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
         * @param optRestaurant can be null
         * @return the Food if there are no errors
         * @throws IllegalStateException
         */
        private static Result<Food> fromFoodResultSet(
            final Connection connection,
            final ResultSet result,
            final Restaurant optRestaurant
        ) throws SQLException {
            final int id = result.getInt("codice");
            final String typeStr = result.getString("tipologia");
            final Result<Optional<FoodType>> tmpType = FoodType.DAO.find(
                connection,
                typeStr
            );
            if (!tmpType.isSuccess()) {
                // Propagate the error
                return Result.failure(tmpType.getErrorMessage());
            }
            if (!tmpType.getValue().isPresent()) {
                final String errorMessage =
                    "The Food have an invalid FoodType: " + typeStr;
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new IllegalStateException(errorMessage);
            }
            final FoodType type = tmpType.getValue().get();

            final Restaurant restaurant;
            if (optRestaurant == null) {
                final String restaurantStr = result.getString("nome_attività");
                final Result<Optional<Restaurant>> tmpRestaurant =
                    Restaurant.DAO.find(connection, restaurantStr);
                if (!tmpRestaurant.isSuccess()) {
                    // Propagate the error
                    return Result.failure(tmpRestaurant.getErrorMessage());
                }
                if (!tmpRestaurant.getValue().isPresent()) {
                    final String errorMessage =
                        "The Food have an invalid Restaurant name: "
                        + restaurantStr;
                    LOGGER.log(Level.SEVERE, errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
                restaurant = tmpRestaurant.getValue().get();
            } else {
                restaurant = optRestaurant;
            }

            final String name = result.getString("nome");
            final BigDecimal price = result.getBigDecimal("prezzo");
            return Result.success(new Food(id, name, restaurant, price, type));
        }

        /**
         * Find in the database the Food with the given name and serverd by the given Restaurant.
         * @param connection
         * @param name
         * @param restaurant
         * @return Optional.of(Food) if it exists, Optional.empty() if no Food was found, error otherwise
         * @throws IllegalStateException if the Food searched exists but the linked FoodType no.
         */
        public static Result<Optional<Food>> find(
            final Connection connection,
            final String name,
            final Restaurant restaurant
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.FIND_FOOD_BY_NAME,
                    name,
                    restaurant.getRestaurantName()
                );
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final Result<Food> resFood = fromFoodResultSet(
                        connection,
                        result,
                        restaurant
                    );
                    if (resFood.isSuccess()) {
                        return Result.success(Optional.of(resFood.getValue()));
                    } else {
                        return Result.failure(resFood.getErrorMessage());
                    }
                } else {
                    return Result.success(Optional.empty());
                }
            } catch (SQLException e) {
                final String errorMessage = "Failed research of Food: " + name;
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Find in the database the Food with the given id.
         * @param connection
         * @param id
         * @return Optional.of(Food) if it exists, Optional.empty() if no Food was found, error otherwise
         * @throws IllegalStateException if the Food searched exists but the linked FoodType no.
         */
        public static Result<Optional<Food>> find(
            final Connection connection,
            final int id
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.FIND_FOOD_BY_ID,
                    id
                );
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final Result<Food> resFood = fromFoodResultSet(
                        connection,
                        result,
                        null
                    );
                    if (resFood.isSuccess()) {
                        return Result.success(Optional.of(resFood.getValue()));
                    } else {
                        // Propagate error
                        return Result.failure(resFood.getErrorMessage());
                    }
                } else {
                    return Result.success(Optional.empty());
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed research of Food with ID: " + id;
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Insert a new Food in the database.
         * @param connection
         * @param name
         * @param restaurant
         * @param price
         * @param type
         * @return the Food if it has been correctly added, empty otherwise
         * @throws IllegalStateException if the retrieval of the record ID fails
         */
        public static Result<Food> insert(
            final Connection connection,
            final String name,
            final Restaurant restaurant,
            final BigDecimal price,
            final FoodType type
        ) {
            final Result<Optional<Food>> food = find(
                connection,
                name,
                restaurant
            );

            if (!food.isSuccess()) {
                // Propagate the error
                return Result.failure(food.getErrorMessage());
            }
            if (food.getValue().isPresent()) {
                final String errorMessage = "Food '" + name + "' (" + restaurant.getRestaurantName()
                    + ") not inserted, it already exists";
                LOGGER.log(Level.WARNING, errorMessage);
                return Result.failure(errorMessage);
            }

            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.INSERT_FOOD,
                    name,
                    restaurant.getRestaurantName(),
                    price,
                    type.getName()
                );
            ) {
                final int rows = statement.executeUpdate();
                if (rows < 1) {
                    final String errorMessage = "Failed food insertion, no rows added";
                    LOGGER.log(Level.SEVERE, errorMessage);
                    return Result.failure(errorMessage);
                } else {
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            final int id = keys.getInt("codice");
                            return Result.success(
                                new Food(id, name, restaurant, price, type)
                            );
                        } else {
                            final String errorMessage =
                                "Insertion of Food seams complete but the retrieval of the record ID failed";
                            LOGGER.log(Level.SEVERE, errorMessage);
                            throw new IllegalStateException(errorMessage);
                        }
                    }
                }
            } catch (SQLException e) {
                final String errorMessage = "Failed insertion of food: " + name + " - " + restaurant.getRestaurantName();
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Lists all Foods served by the given Restaurant.
         * @param connection
         * @param restaurant
         * @return a Collection<Restaurant> if there are no error
         * @throws IllegalStateException if one Food have a non-existent linked FoodType.
         */
        public static Result<Collection<Food>> list(
            final Connection connection,
            final Restaurant restaurant
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.LIST_FOODS,
                    restaurant.getRestaurantName()
                );
                ResultSet result = statement.executeQuery();
            ) {
                final Collection<Food> foods = new HashSet<>();
                while (result.next()) {
                    final Result<Food> resFood = fromFoodResultSet(
                        connection,
                        result,
                        restaurant
                    );
                    if (resFood.isSuccess()) {
                        foods.add(resFood.getValue());
                    } else {
                        return Result.failure(resFood.getErrorMessage());
                    }
                }
                return Result.success(foods);
            } catch (SQLException e) {
                final String errorMessage = "Failed listing restaurants";
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Find the most purchased Food.
         * @param connection
         * @return a pair <Food, Integer> if there are no errors
         * @throws IllegalStateException if the retrieval of the record ID fails
         */
        public static Result<Entry<Food, Integer>> getMostPurchased(
            final Connection connection
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.FIND_FOOD_MOST_PURCHASED
                );
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final Result<Food> resFood = fromFoodResultSet(
                        connection,
                        result,
                        null
                    );
                    if (resFood.isSuccess()) {
                        final int count = result.getInt("quantità_totale");
                        return Result.success(
                            new SimpleImmutableEntry<>(
                                resFood.getValue(),
                                count
                            )
                        );
                    } else {
                        return Result.failure(resFood.getErrorMessage());
                    }
                } else {
                    final String errorMessage =
                        "No value returned from the query";
                    LOGGER.log(Level.WARNING, errorMessage);
                    return Result.failure(errorMessage);
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed getting most purchased Food";
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }
    }
}
