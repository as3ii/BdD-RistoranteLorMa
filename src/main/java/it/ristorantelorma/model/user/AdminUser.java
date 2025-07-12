package it.ristorantelorma.model.user;

/**
 * Represent an entry in the USERS table of the database with role = Role.ADMIN.
 */
public final class AdminUser extends User {

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
    AdminUser(
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
     * {@inheritDoc}
     */
    @Override
    public Role getRole() {
        return Role.ADMIN;
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
    }
}
