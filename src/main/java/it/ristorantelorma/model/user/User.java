package it.ristorantelorma.model.user;

import it.ristorantelorma.controller.SimpleLogger;
import it.ristorantelorma.model.DBHelper;
import it.ristorantelorma.model.Queries;
import it.ristorantelorma.model.Result;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represent an entry in the USERS table of the database.
 */
public abstract class User {

    private final String name;
    private final String surname;
    private final String username;
    private final String password;
    private final String phone;
    private final String email;
    private final String city;
    private final String street;
    private final String houseNumber;

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
    User(
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
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.city = city;
        this.street = street;
        this.houseNumber = houseNumber;
    }

    /**
     * @return first name
     */
    public String getName() {
        return name;
    }

    /**
     * @return last name
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return encrypted password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return phone number with optional prefix
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return city
     */
    public String getCity() {
        return city;
    }

    /**
     * @return street
     */
    public String getStreet() {
        return street;
    }

    /**
     * @return house number
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * @return Role
     */
    public abstract Role getRole();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other instanceof User) {
            final User u = (User) other;
            return u.username.equals(this.username);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%s = { name = \"%s\", surname = \"%s\", username = \"%s\", phone = \"%s\", "
            + "email = \"%s\", city = \"%s\", street = \"%s\", houseNumber = \"%s\"}",
            this.getClass().getSimpleName(),
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
     * Inner class that handles requests to the database.
     */
    public static final class DAO {

        private static final String CLASS_NAME = DAO.class.getName();
        private static final Logger LOGGER = SimpleLogger.getLogger(CLASS_NAME);
        private static final BigDecimal DEFAULT_CREDIT = new BigDecimal(20);

        private DAO() {
            throw new UnsupportedOperationException(
                "Utility class and cannot be instantiated"
            );
        }

        /**
         * Insert a new User in the database.
         * If Role is Role.CLIENT then the new User have a credit of DEFAULT_CREDIT
         * @param connection
         * @param name
         * @param surname
         * @param username
         * @param password      the password MUST be encrypted beforehand
         * @param phone
         * @param email
         * @param city
         * @param street
         * @param houseNumber
         * @param role
         * @return the User if it has been correctly added, error otherwise
         */
        public static Result<User> insert(
            final Connection connection,
            final String name,
            final String surname,
            final String username,
            final String password,
            final String phone,
            final String email,
            final String city,
            final String street,
            final String houseNumber,
            final Role role
        ) {
            final Result<Optional<User>> user;
            try {
                user = find(connection, username);
            } catch (IllegalArgumentException e) {
                // Error already logged, just return the error
                return Result.failure(
                    "Error while checking if exists a user with username: "
                    + username
                );
            }

            if (!user.isSuccess()) {
                // Propagate the error
                return Result.failure(user.getErrorMessage());
            }
            if (user.getValue().isPresent()) {
                final String errorMessage =
                    "User '" + username + "' not inserted, it already exists";
                LOGGER.log(Level.WARNING, errorMessage);
                return Result.failure(errorMessage);
            }

            BigDecimal credit = null;
            if (role == Role.CLIENT) {
                credit = DEFAULT_CREDIT;
            } else if (role == Role.DELIVERYMAN) {
                credit = BigDecimal.ZERO;
            }
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.INSERT_USER,
                    name,
                    surname,
                    username,
                    password,
                    phone,
                    email,
                    city,
                    street,
                    houseNumber,
                    credit,
                    role.toSQLStr()
                );
            ) {
                final int rows = statement.executeUpdate();
                if (rows < 1) {
                    final String errorMessage =
                        "Failed user insertion, no rows added";
                    LOGGER.log(Level.SEVERE, errorMessage);
                    return Result.failure(errorMessage);
                } else {
                    final User newUser;
                    switch (role) {
                        case ADMIN:
                            newUser = new AdminUser(
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
                            break;
                        case CLIENT:
                            newUser = new ClientUser(
                                name,
                                surname,
                                username,
                                password,
                                phone,
                                email,
                                city,
                                street,
                                houseNumber,
                                credit
                            );
                            break;
                        case DELIVERYMAN:
                            newUser = new DeliverymanUser(
                                name,
                                surname,
                                username,
                                password,
                                phone,
                                email,
                                city,
                                street,
                                houseNumber,
                                credit
                            );
                            break;
                        case RESTAURANT:
                            newUser = new RestaurantUser(
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
                            break;
                        default:
                            return Result.failure("Invalid role");
                    }
                    return Result.success(newUser);
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed insertion of user: " + username;
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Find a User by its username.
         * @param connection
         * @param username
         * @return Optional.of(User) if it was found, Optional.empty() if no User was found, error message otherwise
         * @throws IllegalArgumentException if an invalid role enum is returned from the query
         * @throws IllegalStateException if client or deliveryman have credit = null
         */
        public static Result<Optional<User>> find(
            final Connection connection,
            final String username
        ) {
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.FIND_USER,
                    username
                );
                ResultSet result = statement.executeQuery();
            ) {
                if (result.next()) {
                    final String name = result.getString("nome");
                    final String surname = result.getString("cognome");
                    final String password = result.getString("password");
                    final String phone = result.getString("telefono");
                    final String email = result.getString("email");
                    final String city = result.getString("città");
                    final String street = result.getString("via");
                    final String houseNumber = result.getString("n_civico");
                    final Optional<BigDecimal> credit = Optional.ofNullable(
                        result.getBigDecimal("credito")
                    );
                    final Role role = Role.fromString(
                        result.getString("ruolo")
                    );
                    final User newUser;
                    switch (role) {
                        case ADMIN:
                            newUser = new AdminUser(
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
                            break;
                        case CLIENT:
                            if (credit.isEmpty()) {
                                throw new IllegalStateException(
                                    "Role cannot be 'client' with empty credit"
                                );
                            }
                            newUser = new ClientUser(
                                name,
                                surname,
                                username,
                                password,
                                phone,
                                email,
                                city,
                                street,
                                houseNumber,
                                credit.get()
                            );
                            break;
                        case DELIVERYMAN:
                            if (credit.isEmpty()) {
                                throw new IllegalStateException(
                                    "Role cannot be 'deliveryman' with empty credit"
                                );
                            }
                            newUser = new DeliverymanUser(
                                name,
                                surname,
                                username,
                                password,
                                phone,
                                email,
                                city,
                                street,
                                houseNumber,
                                credit.get()
                            );
                            break;
                        case RESTAURANT:
                            newUser = new RestaurantUser(
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
                            break;
                        default:
                            return Result.failure("Invalid role");
                    }
                    return Result.success(Optional.of(newUser));
                } else {
                    return Result.success(Optional.empty());
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed research of User: " + username;
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }

        /**
         * Set the credit for the given ClientUser or DeliverymanUser.
         * @param connection
         * @param user
         * @param credit
         * @return Success (empty) if the update succede, error otherwise
         * @throws IllegalArgumentException if the given user is not a client or deliveryman
         */
        static Result<?> updateCredit(
            final Connection connection,
            final User user,
            final BigDecimal credit
        ) {
            Objects.requireNonNull(credit); // Avoid setting the field to null in the DB
            if (!(user instanceof ClientUser || user instanceof DeliverymanUser)) {
                throw new IllegalArgumentException(
                    "The user is not a ClientUser or a DeliverymanUser"
                );
            }
            try (
                PreparedStatement statement = DBHelper.prepare(
                    connection,
                    Queries.SET_USER_CREDIT,
                    credit,
                    user.getUsername()
                );
            ) {
                final int rows = statement.executeUpdate();
                if (rows < 1) {
                    final String errorMessage =
                        "Failed user's credit update, no rows changed";
                    LOGGER.log(Level.SEVERE, errorMessage);
                    return Result.failure(errorMessage);
                } else {
                    return Result.success(new Object()); // Return dummy value
                }
            } catch (SQLException e) {
                final String errorMessage =
                    "Failed updating user's credit, username: "
                    + user.getUsername();
                LOGGER.log(Level.SEVERE, errorMessage, e);
                return Result.failure(errorMessage);
            }
        }
    }
}
