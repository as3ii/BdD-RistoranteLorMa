package it.ristorantelorma.model.user;

import it.ristorantelorma.controller.SimpleLogger;
import it.ristorantelorma.model.DBHelper;
import it.ristorantelorma.model.Queries;
import it.ristorantelorma.model.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represent an entry in the USERS table of the database with role = Role.DELIVERYMAN.
 */
public final class DeliverymanUser extends User {

    /**
     * @param name          the first name of the User
     * @param surname       the last name of the User
     * @param username
     * @param password      the encrypted password
     * @param phone         the phone number with optional prefix
     * @param email
     * @param city
     * @param street
     * @param houseNumber
     */
    DeliverymanUser(
        final String name,
        final String surname,
        final String username,
        final String password,
        final String phone,
        final String email,
        final String city,
        final String street,
        final String houseNumber
    ) {
        super(
            name,
            surname,
            username,
            password,
            phone,
            email,
            city,
            street,
            houseNumber
        );
    }

    /**
     * @param user       the user MUST be an instance of DeliverymanUser
     */
    private DeliverymanUser(final User user) {
        super(
            user.getName(),
            user.getSurname(),
            user.getUsername(),
            user.getPassword(),
            user.getPhone(),
            user.getEmail(),
            user.getCity(),
            user.getStreet(),
            user.getHouseNumber()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Role getRole() {
        return Role.DELIVERYMAN;
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
         * Find a DeliverymanUser by its username.
         * @param connection
         * @param username
         * @return Optional.of(User) if it was found, Optional.empty() if no User was found, error message otherwise
         * @throws IllegalArgumentException if an invalid role enum is returned from the query
         */
        public static Result<Optional<DeliverymanUser>> find(
            final Connection connection,
            final String username
        ) {
            final Result<Optional<User>> tmpUser = User.DAO.find(
                connection,
                username
            );
            if (!tmpUser.isSuccess()) {
                // Propagate error
                return Result.failure(tmpUser.getErrorMessage());
            }
            if (tmpUser.getValue().isEmpty()) {
                return Result.success(Optional.empty());
            }
            final User user = tmpUser.getValue().get();
            if (!(user instanceof DeliverymanUser)) {
                return Result.failure("The searched user is not a deliveryman");
            }

            return Result.success(Optional.of(new DeliverymanUser(user)));
        }

        /**
         * Get the DeliverymanUser with more deliveries.
         * @param connection
         * @return a pair <DeliverymanUser, Integer> if there are no errors
         * @throws IllegalArgumentException if an invalid Role enum is returned from the query
         * @throws IllegalStateException if the returned user is not a Deliveryman
         */
        public static Result<
            Entry<DeliverymanUser, Integer>
        > getTopByDeliveryCount(final Connection connection) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.FIND_DELIVERYMAN_WITH_MORE_DELIVERIES
                );
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final String roleStr = result.getString("ruolo");
                    final Role role = Role.fromString(roleStr);
                    if (role != Role.DELIVERYMAN) {
                        throw new IllegalStateException(
                            "Unexpected role " + roleStr
                        );
                    }
                    final String username = result.getString("username");
                    final String name = result.getString("nome");
                    final String surname = result.getString("cognome");
                    final String password = result.getString("password");
                    final String phone = result.getString("telefono");
                    final String email = result.getString("email");
                    final String city = result.getString("città");
                    final String street = result.getString("via");
                    final String houseNumber = result.getString("n_civico");
                    final int count = result.getInt("numero_ordini");
                    return Result.success(
                        new SimpleImmutableEntry<>(
                            new DeliverymanUser(
                                name,
                                surname,
                                username,
                                password,
                                phone,
                                email,
                                city,
                                street,
                                houseNumber
                            ),
                            count
                        )
                    );
                } else {
                    return Result.failure("No deliveryman found");
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed getting the deliveryman with more deliveries";
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }
    }
}
