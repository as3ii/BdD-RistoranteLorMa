package it.ristorantelorma.model;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Objects;

/**
 * Represent an entry in the USERS table of the database.
 */
public final class User {
    private final String name;
    private final String surname;
    private final String username;
    private final String password;
    private final String phone;
    private final String email;
    private final String city;
    private final String street;
    private final String houseNumber;
    private final Optional<BigDecimal> credit;
    private final Role role;

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
     * @param credit        if role is Role.CLIENT this is the client credit, empty otherwise
     * @param role          the Role of the User
     */
    public User(
        final String name, final String surname, final String username,
        final String password, final String phone, final String email,
        final String city, final String street, final String houseNumber,
        final Optional<BigDecimal> credit, final Role role
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
        this.credit = credit;
        this.role = role;
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
     * @return the credit if role is Role.CLIENT, empty otherwise
     */
    public Optional<BigDecimal> getCredit() {
        return credit;
    }

    private String getCreditString() {
        if (credit.isPresent()) {
            return credit.toString();
        } else {
            return "None";
        }
    }

    /**
     * @return Role
     */
    public Role getRole() {
        return role;
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
            "User = { name = \"%s\", surname = \"%s\", username = \"%s\", phone = \"%s\", "
            + "email = \"%s\", city = \"%s\", street = \"%s\", houseNumber = \"%s\", "
            + "credit = %s, role = \"%s\" }",
            name,
            surname,
            username,
            password,
            phone,
            email,
            city,
            street,
            houseNumber,
            getCreditString(),
            role
        );
    }

    /**
     * Inner class that handles requests to the database.
     */
    public static final class DAO { }
}
