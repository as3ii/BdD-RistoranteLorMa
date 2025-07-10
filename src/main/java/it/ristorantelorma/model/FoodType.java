package it.ristorantelorma.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.ristorantelorma.controller.SimpleLogger;

/**
 * Represent an entry in the FOOD_TYPES table of the database.
 */
public final class FoodType {
    private final String name;
    private final MacroType type;

    /**
     * @param name the name of the type
     * @param type @see MacroType
     */
    public FoodType(final String name, final MacroType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @return Food type name
     */
    public String getName() {
        return name;
    }

    /**
     * @return @see MacroType
     */
    public MacroType getType() {
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
        } else if (other instanceof FoodType) {
            final FoodType t = (FoodType) other;
            return t.name.equals(this.name);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
         return Objects.hash(this.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "FoodType = { name = \"%s\", type = \"%s\" }",
            name,
            type
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
         * Find a FoodType in the database by its name.
         * @param connection
         * @param name
         * @return Optional.of(FoodType) if it was found, Optional.empty() if no FoodType was found, error otherwise
         * @throws IllegalArgumentException if an invalid MacroType enum is returned from the query
         */
        public static Result<Optional<FoodType>> find(final Connection connection, final String name) {
            try (
                PreparedStatement statement = DBHelper.prepare(connection, Queries.FIND_FOOD_TYPE, name);
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final String typeStr = result.getString("tipologia");
                    final MacroType type;
                    switch (typeStr) {
                        case "cibo":
                            type = MacroType.DISH;
                            break;
                        case "bevanda":
                            type = MacroType.DRINK;
                            break;
                        default:
                            final String errorMessage = "Invalid MacroType value: " + typeStr;
                            LOGGER.log(Level.SEVERE, errorMessage);
                            throw new IllegalArgumentException(errorMessage);
                    }
                    return Result.success(Optional.of(
                        new FoodType(name, type)
                    ));
                } else {
                    return Result.success(Optional.empty());
                }
            } catch (SQLException e) {
                final String errorMessage = "Failed research of FoodType: " + name;
                LOGGER.log(Level.SEVERE, errorMessage);
                return Result.failure(errorMessage);
            }
        }

        /**
         * List all FoodTypes in the database.
         * @param connection
         * @return Collection<FoodType> if no error is encountered, error otherwise
         * @throws IllegalArgumentException if an invalid MacroType enum is returned from the query
         */
        public static Result<Collection<FoodType>> list(final Connection connection) {
            try (
                PreparedStatement statement = DBHelper.prepare(connection, Queries.LIST_FOOD_TYPES);
                ResultSet result = statement.executeQuery();
            ) {
                final Collection<FoodType> foodTypes = new HashSet<>();
                while (result.next()) {
                    final String name = result.getString("nome");
                    final String typeStr = result.getString("tipologia");
                    final MacroType type;
                    switch (typeStr) {
                        case "cibo":
                            type = MacroType.DISH;
                            break;
                        case "bevanda":
                            type = MacroType.DRINK;
                            break;
                        default:
                            final String errorMessage = "Invalid MacroType value: " + typeStr;
                            LOGGER.log(Level.SEVERE, errorMessage);
                            throw new IllegalArgumentException(errorMessage);
                    }
                    foodTypes.add(new FoodType(name, type));
                }
                return Result.success(foodTypes);
            } catch (SQLException e) {
                final String errorMessage = "Failed listing FoodTypes";
                LOGGER.log(Level.SEVERE, errorMessage);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Insert a new FoodType in the database.
         * @param connection
         * @param name
         * @param type
         * @return the FoodType if it hs been correctly added, error otherwise
         */
        public static Result<FoodType> insert(
            final Connection connection, final String name, final MacroType type
        ) {
            final Result<Optional<FoodType>> foodType;
            try {
                foodType = find(connection, name);
            } catch (IllegalArgumentException e) {
                // Error already logged, just return the error
                return Result.failure("Error while checking if exists a FoodType with name: " + name);
            }

            if (!foodType.isSuccess()) {
                // Propagate the error
                return Result.failure(foodType.getErrorMessage());
            }
            if (foodType.getValue().isEmpty()) {
                final String errorMessage = "FoodType '" + name + "' not inserted, it already exists";
                LOGGER.log(Level.WARNING, errorMessage);
                return Result.failure(errorMessage);
            }

            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection, Queries.INSERT_FOOD_TYPE, name, type
                );
            ) {
                final int rows = statement.executeUpdate();
                if (rows < 1) {
                    final String errorMessage = "Failed FoodType insertion, no rows added";
                    LOGGER.log(Level.SEVERE, errorMessage);
                    return Result.failure(errorMessage);
                } else {
                    return Result.success(new FoodType(name, type));
                }
            } catch (SQLException e) {
                final String errorMessage = "Failed insertion of FoodType: " + name;
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }
    }
}
