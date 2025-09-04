package it.ristorantelorma.model.user;

import it.ristorantelorma.model.Result;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Objects;
import java.util.Optional;

/**
 * Represent an entry in the USERS table of the database with role = Role.CLIENT.
 */
public final class ClientUser extends User {

    private BigDecimal credit;

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
     * @param credit
     */
    public ClientUser(
        final String name,
        final String surname,
        final String username,
        final String password,
        final String phone,
        final String email,
        final String city,
        final String street,
        final String houseNumber,
        final BigDecimal credit
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
        this.credit = credit;
    }

    /**
     * @param user       the user MUST be an instance of ClientUser
     * @param credit
     */
    private ClientUser(final User user, final BigDecimal credit) {
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
        this.credit = credit;
    }

    /**
     * @return the client balance
     */
    public BigDecimal getCredit() {
        return credit;
    }

    /**
     * Set client's credit. This MUST remain private.
     * @param newCredit
     */
    private void setCredit(final BigDecimal newCredit) {
        this.credit = newCredit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Role getRole() {
        return Role.CLIENT;
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
         * Find a ClientUser by its username.
         * @param connection
         * @param username
         * @return Optional.of(User) if it was found, Optional.empty() if no User was found, error message otherwise
         * @throws IllegalArgumentException if an invalid role enum is returned from the query
         */
        public static Result<Optional<ClientUser>> find(
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
            if (!(user instanceof ClientUser)) {
                return Result.failure("The searched user is not a client");
            }

            return Result.success(
                Optional.of(
                    new ClientUser(user, ((ClientUser) user).getCredit())
                )
            );
        }

        /**
         * Set the credit for the given ClientUser.
         * @param connection
         * @param user
         * @param credit
         * @return the updated ClientUser
         */
        public static Result<ClientUser> updateCredit(
            final Connection connection,
            final ClientUser user,
            final BigDecimal credit
        ) {
            Objects.requireNonNull(credit); // Avoid setting the field to null in the DB
            final Result<?> result = User.DAO.updateCredit(connection, user, credit);
            if (result.isSuccess()) {
                user.setCredit(credit);
                return Result.success(user);
            } else {
                return Result.failure(result.getErrorMessage());
            }
        }
    }
}
