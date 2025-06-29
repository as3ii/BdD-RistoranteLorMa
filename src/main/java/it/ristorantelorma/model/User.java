package it.ristorantelorma.model;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Objects;

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

    @Override
    public int hashCode() {
         return Objects.hash(this.username);
    }

    //public String toString() { }

    public static final class DAO { }

    public enum Role {
        ADMIN,
        RESTAURANT,
        CLIENT,
        DELIVERYMAN;
    }
}
