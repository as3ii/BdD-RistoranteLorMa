package it.ristorantelorma.model.order;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.ristorantelorma.controller.SimpleLogger;
import it.ristorantelorma.model.DBHelper;
import it.ristorantelorma.model.Food;
import it.ristorantelorma.model.Queries;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.user.ClientUser;
import it.ristorantelorma.model.user.DeliverymanUser;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represent an entry in the ORDERS table of the database.
 */
public abstract class Order {

    private final int id;
    private final Restaurant restaurant;
    private final Timestamp dateTime;
    private final BigDecimal shippingRate;
    private final ClientUser client;
    private final Map<Food, Integer> foodRequested;

    /**
     * @param id                the ID of the record in the database
     * @param restaurant        the Restaurant to which the order was placed
     * @param dateTime          when the order was created
     * @param shippingRate      the price for the shipment
     * @param client            the client that placed the order
     * @param foodRequested     the food requested by the client
     */
    Order(
        final int id,
        final Restaurant restaurant,
        final Timestamp dateTime,
        final BigDecimal shippingRate,
        final ClientUser client,
        final Map<Food, Integer> foodRequested
    ) {
        this.id = id;
        this.restaurant = restaurant;
        this.dateTime = new Timestamp(dateTime.getTime());
        this.shippingRate = shippingRate;
        this.client = client;
        this.foodRequested = new HashMap<>(foodRequested);
    }

    /**
     * @return ID of the record in the database
     */
    public int getId() {
        return id;
    }

    /**
     * @return Restaurant linked to the order
     */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     * @return when the order was created
     */
    public Timestamp getDateTime() {
        return new Timestamp(dateTime.getTime());
    }

    /**
     * @return the price for the shipment
     */
    public BigDecimal getShippingRate() {
        return shippingRate;
    }

    /**
     * @return the User of the client
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "The client's credit can be mutated without issues"
    )
    public ClientUser getClient() {
        return client;
    }

    /**
     * @return the food requested by the client
     */
    public Map<Food, Integer> getFoodRequested() {
        return Map.copyOf(foodRequested);
    }

    private String getFoodRequestedString() {
        return foodRequested
            .entrySet()
            .stream()
            .map(e -> "\"" + e.getKey() + "\" = " + e.getValue())
            .collect(Collectors.joining(", ", "{ ", " }"));
    }

    /**
     * @return the state of the Order
     */
    public abstract State getState();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other instanceof Order) {
            final Order o = (Order) other;
            return o.id == this.id;
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
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss"
        );
        return String.format(
            "%s = { id = %d, client = \"%s\", restaurant = \"%s\", "
            + "dateTime = %s, shippingRate = %.2f, state = \"%s\", foodRequested = %s }",
            this.getClass().getSimpleName(),
            id,
            client.getUsername(),
            restaurant.getRestaurantName(),
            dateTime.toLocalDateTime().format(formatter),
            shippingRate,
            getState(),
            getFoodRequestedString()
        );
    }

    /**
     * Inner class that handles requests to the database.
     */
    public static final class DAO {

        private static final String CLASS_NAME = Order.class.getName();
        private static final Logger LOGGER = SimpleLogger.getLogger(CLASS_NAME);

        private DAO() {
            throw new UnsupportedOperationException(
                "Utility class and cannot be instantiated"
            );
        }

        /**
         *
         * @param connection
         * @param id
         * @return Map<Food,Integer> if no error is encountered, error otherwise
         */
        public static Result<Map<Food, Integer>> getFoodList(
            final Connection connection,
            final int id
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.LIST_FOODS_BY_ORDER_ID,
                    id
                );
                ResultSet result = statement.executeQuery();
            ) {
                final Map<Food, Integer> foods = new HashMap<>();
                while (result.next()) {
                    final int quantity = result.getInt("quantità");
                    final int foodId = result.getInt("codice_vivanda");
                    final Result<Optional<Food>> tmpFood = Food.DAO.find(
                        connection,
                        foodId
                    );
                    if (!tmpFood.isSuccess()) {
                        // Propagate the error
                        return Result.failure(tmpFood.getErrorMessage());
                    }
                    if (!tmpFood.getValue().isPresent()) {
                        final String errorMessage =
                            "Invalid Food id: " + foodId;
                        LOGGER.log(Level.SEVERE, errorMessage);
                        return Result.failure(errorMessage);
                    }
                    final Food food = tmpFood.getValue().get();
                    foods.put(food, quantity);
                }
                return Result.success(foods);
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed listing foods for Order ID: " + id;
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Insert (batch) the pairs (food,count) for the given orderId.
         * @param connection
         * @param orderId
         * @param foods
         * @return the input foods if everything goes well, error otherwise
         */
        public static Result<Map<Food, Integer>> insertFoodRequested(
            final Connection connection,
            final int orderId,
            final Map<Food, Integer> foods
        ) {
            final Iterator<Entry<Food, Integer>> iter = foods
                .entrySet()
                .iterator();
            boolean autocommit = true;

            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.INSERT_ORDER_DETAIL
                );
            ) {
                autocommit = connection.getAutoCommit();
                connection.setAutoCommit(false); // Temporary disable autocommit
                while (iter.hasNext()) {
                    final Entry<Food, Integer> element = iter.next();
                    final Food food = element.getKey();
                    final Integer count = element.getValue();
                    statement.setInt(1, food.getId());
                    statement.setInt(2, orderId);
                    statement.setInt(3, count);
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
                connection.setAutoCommit(autocommit); // Restore autocommit

                return Result.success(foods);
            } catch (SQLException e) {
                try {
                    connection.rollback();
                    LOGGER.log(
                        Level.WARNING,
                        "Rolled back transaction for orderId: " + orderId
                    );
                } catch (SQLException er) {
                    LOGGER.log(
                        Level.SEVERE,
                        "Failed rollback while handling an exception",
                        er
                    );
                }
                final String errorMessage =
                    "Failed batch insertion of food requested for orderId: "
                    + orderId;
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            } finally {
                try {
                    connection.setAutoCommit(autocommit); // Restore autocommit
                } catch (SQLException e) {
                    LOGGER.log(
                        Level.SEVERE,
                        "Failed setting back autocommit",
                        e
                    );
                }
            }
        }

        /**
         * Find in the database the Order with the given id.
         * @param connection
         * @param id
         * @return Optional.of(Order) if it exists, Optional.empty() if no Order was found, error otherwise
         * @throws IllegalStateException if the Order searched exists but the linked Restaurant name,
         *         client username, or deliveryman username do not.
         * @throws IllegalArgumentException if an invalid Status enum is returned from the query
         */
        public static Result<Optional<Order>> find(
            final Connection connection,
            final int id
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.FIND_ORDER_BY_ID,
                    id
                );
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final String restaurantStr = result.getString(
                        "nome_attività"
                    );
                    final Result<Optional<Restaurant>> tmpRestaurant =
                        Restaurant.DAO.find(connection, restaurantStr);
                    if (!tmpRestaurant.isSuccess()) {
                        // Propagate the error
                        return Result.failure(tmpRestaurant.getErrorMessage());
                    }
                    if (!tmpRestaurant.getValue().isPresent()) {
                        final String errorMessage =
                            "The Order have an invalid Restaurant name: "
                            + restaurantStr;
                        LOGGER.log(Level.SEVERE, errorMessage);
                        throw new IllegalStateException(errorMessage);
                    }
                    final Restaurant restaurant = tmpRestaurant
                        .getValue()
                        .get();

                    final String clientStr = result.getString(
                        "username_cliente"
                    );
                    final Result<Optional<ClientUser>> tmpClient = ClientUser.DAO.find(
                        connection,
                        clientStr
                    );
                    if (!tmpClient.isSuccess()) {
                        // Propagate the error
                        return Result.failure(tmpClient.getErrorMessage());
                    }
                    if (!tmpClient.getValue().isPresent()) {
                        final String errorMessage =
                            "The Order have an invalid client's username: "
                            + clientStr;
                        LOGGER.log(Level.SEVERE, errorMessage);
                        throw new IllegalStateException(errorMessage);
                    }
                    final ClientUser client = tmpClient.getValue().get();

                    final Optional<String> deliverymanStr = Optional.ofNullable(
                        result.getString("username_fattorino")
                    );
                    final Optional<DeliverymanUser> deliveryman;
                    if (deliverymanStr.isPresent()) {
                        final Result<Optional<DeliverymanUser>> tmpDeliveryman =
                            DeliverymanUser.DAO.find(connection, deliverymanStr.get());
                        if (!tmpDeliveryman.isSuccess()) {
                            // Propagate the error
                            return Result.failure(
                                tmpDeliveryman.getErrorMessage()
                            );
                        }
                        if (!tmpDeliveryman.getValue().isPresent()) {
                            final String errorMessage =
                                "The Order have an invalid deliveryman's username: "
                                + deliverymanStr.get();
                            LOGGER.log(Level.SEVERE, errorMessage);
                            throw new IllegalStateException(errorMessage);
                        }
                        deliveryman = tmpDeliveryman.getValue();
                    } else {
                        deliveryman = Optional.empty();
                    }

                    final Result<Map<Food, Integer>> tmpFoodRequested =
                        getFoodList(connection, id);
                    if (!tmpFoodRequested.isSuccess()) {
                        // Propagate the error
                        return Result.failure(tmpClient.getErrorMessage());
                    }
                    final Map<Food, Integer> foodRequested =
                        tmpFoodRequested.getValue();

                    final Timestamp dateTime = result.getTimestamp("data_ora");
                    final BigDecimal shippingRate = result.getBigDecimal(
                        "tariffa_spedizione"
                    );
                    final Optional<Timestamp> acceptanceTime =
                        Optional.ofNullable(
                            result.getTimestamp("ora_accettazione")
                        );
                    final Optional<Timestamp> deliveryTime =
                        Optional.ofNullable(
                            result.getTimestamp("ora_consegna")
                        );

                    final String stateStr = result.getString("stato");
                    switch (State.fromString(stateStr)) {
                        case State.WAITING:
                            return Result.success(
                                Optional.of(
                                    new WaitingOrder(
                                        id,
                                        restaurant,
                                        dateTime,
                                        shippingRate,
                                        client,
                                        foodRequested
                                    )
                                )
                            );
                        case State.READY:
                            return Result.success(
                                Optional.of(
                                    new ReadyOrder(
                                        id,
                                        restaurant,
                                        dateTime,
                                        shippingRate,
                                        client,
                                        foodRequested
                                    )
                                )
                            );
                        case State.ACCEPTED:
                            if (
                                acceptanceTime.isEmpty()
                                || deliveryman.isEmpty()
                            ) {
                                throw new IllegalStateException(
                                    "State cannot be 'accepted' with empty acceptanceTime or deliveryman"
                                );
                            }
                            return Result.success(
                                Optional.of(
                                    new AcceptedOrder(
                                        id,
                                        restaurant,
                                        dateTime,
                                        shippingRate,
                                        client,
                                        foodRequested,
                                        acceptanceTime.get(),
                                        deliveryman.get()
                                    )
                                )
                            );
                        case State.DELIVERED:
                            if (
                                acceptanceTime.isEmpty()
                                || deliveryman.isEmpty()
                                || deliveryTime.isEmpty()
                            ) {
                                throw new IllegalStateException(
                                    "State cannot be 'accepted' with empty acceptanceTime, deliveryman or deliveryTime"
                                );
                            }
                            return Result.success(
                                Optional.of(
                                    new DeliveredOrder(
                                        id,
                                        restaurant,
                                        dateTime,
                                        shippingRate,
                                        client,
                                        foodRequested,
                                        acceptanceTime.get(),
                                        deliveryman.get(),
                                        deliveryTime.get()
                                    )
                                )
                            );
                        case State.CANCELLED:
                            return Result.success(
                                Optional.of(
                                    new CancelledOrder(
                                        id,
                                        restaurant,
                                        dateTime,
                                        shippingRate,
                                        client,
                                        foodRequested,
                                        acceptanceTime,
                                        deliveryman,
                                        deliveryTime
                                    )
                                )
                            );
                        default:
                            return Result.failure("Invalid state");
                    }
                } else {
                    return Result.success(Optional.empty());
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed research of Order with ID: " + id;
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Insert a new Order in the database.
         * @param connection
         * @param restaurant
         * @param dateTime
         * @param shippingRate
         * @param client
         * @param foodRequested
         * @return the WaitingOrder if it has benn correctly added, empty otherwise
         * @throws IllegalStateException if the retrival of the record ID fails
         */
        public static Result<WaitingOrder> insert(
            final Connection connection,
            final Restaurant restaurant,
            final Timestamp dateTime,
            final BigDecimal shippingRate,
            final ClientUser client,
            final Map<Food, Integer> foodRequested
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.INSERT_ORDER,
                    restaurant.getRestaurantName(),
                    dateTime,
                    shippingRate,
                    client.getUsername()
                );
            ) {
                final int rows = statement.executeUpdate();
                if (rows < 1) {
                    final String errorMessage =
                        "Failed Order insertion, no rows added";
                    LOGGER.log(Level.SEVERE, errorMessage);
                    return Result.failure(errorMessage);
                } else {
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            final int id = keys.getInt("codice");

                            return Result.success(
                                new WaitingOrder(
                                    id,
                                    restaurant,
                                    dateTime,
                                    shippingRate,
                                    client,
                                    foodRequested
                                )
                            );
                        } else {
                            final String errorMessage =
                                "Insertion of Order seams complete but the retrival of the record ID failed";
                            LOGGER.log(Level.SEVERE, errorMessage);
                            throw new IllegalStateException(errorMessage);
                        }
                    }
                }
            } catch (SQLException e) {
                final String errorMessage = "Failed insertion of the order";
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Generic Order state updater.
         * @param connection
         * @param order         the order to update
         * @param state         the target state
         * @param objects       query-dependent parameters
         * @return the input Order if everything goes right, error otherwise.
         */
        @SuppressFBWarnings(
            value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
            justification = "SQL string selected via switch statement between static strings"
        )
        static Result<Order> updateState(
            final Connection connection,
            final Order order,
            final State state,
            final Object... objects
        ) {
            final String sql;
            switch (state) {
                case ACCEPTED:
                    sql = Queries.SET_ORDER_ACCEPTED;
                    break;
                case CANCELLED:
                    sql = Queries.SET_ORDER_CANCELLED;
                    break;
                case DELIVERED:
                    sql = Queries.SET_ORDER_DELIVERED;
                    break;
                case READY:
                    sql = Queries.SET_ORDER_READY;
                    break;
                default:
                    return Result.failure("Cannot switch to state " + state);
            }
            final List<Object> args = new ArrayList<>(Arrays.asList(objects));
            args.add(order.getId());
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    sql,
                    args.toArray()
                );
            ) {
                final int rows = statement.executeUpdate();
                if (rows < 1) {
                    final String errorMessage =
                        "Failed order update, no rows changed";
                    LOGGER.log(Level.SEVERE, errorMessage);
                    return Result.failure(errorMessage);
                } else {
                    return Result.success(order);
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed updating order: " + order.getId();
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }
    }
}
