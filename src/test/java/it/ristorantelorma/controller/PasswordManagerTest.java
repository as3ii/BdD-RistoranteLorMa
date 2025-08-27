package it.ristorantelorma.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;

class PasswordManagerTest {

    private static final String PASSWORD = "P@ssw0rd";
    private static final byte[] SALT = "012345678901234567".getBytes(Charset.defaultCharset());
    private static final String HASHED_PASSWORD = "MDEyMzQ1Njc4OTAxMjM0NTY3:".concat(
            "QAkP4UwIf5g/rOA5m2T1Co56iMSbDsdPlNf3yhkevscb/oMkMFALVivZlkJRJksNgzNCnGk0v8D0Anxzm6dcueJqZQtbGlJ6e7JX");

    @Test
    void testEncodePassword() {
        assertEquals(PasswordManager.encodePassword(PASSWORD, SALT), HASHED_PASSWORD);
    }

    @Test
    void testCheckPassword() {
        assertTrue(PasswordManager.checkPassword(PASSWORD, HASHED_PASSWORD));
    }

    @Test
    void testNewEncodedPassword() {
        assertTrue(PasswordManager.checkPassword(PASSWORD,
                PasswordManager.newEncodedPassword(PASSWORD)));
    }

}
