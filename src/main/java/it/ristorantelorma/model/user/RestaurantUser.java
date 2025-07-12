package it.ristorantelorma.model.user;

import it.ristorantelorma.model.Result;
import java.sql.Connection;
import java.util.Optional;

/**
 * Represent an entry in the USERS table of the database with role = Role.RESTAURANT.
 */
public final class RestaurantUser extends User {

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
    RestaurantUser(
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
     * @param user       the user MUST be an instance of RestaurantUser
     */
    private RestaurantUser(final User user) {
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
        return Role.RESTAURANT;
    }

    /**
     * Inner class that handles requests to the database.
     */
    public static final class DAO {

        private DAO() {
            throw new UnsupportedOperationException(
                "Utility class and cannot be instantiated"
            );
        }

        /**
         * Find a RestaurantUser by its username.
         * @param connection
         * @param username
         * @return Optional.of(User) if it was found, Optional.empty() if no User was found, error message otherwise
         * @throws IllegalArgumentException if an invalid role enum is returned from the query
         */
        public static Result<Optional<RestaurantUser>> find(
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
            if (!(user instanceof RestaurantUser)) {
                return Result.failure(
                    "The searched user is not a restaurant owner"
                );
            }

            return Result.success(Optional.of(new RestaurantUser(user)));
        }
    }
}
