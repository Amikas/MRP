// File: test/util/PasswordHasherTest.java
package mrp.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    @Test
    void testHashAndVerify() {
        String password = "mySecretPassword123";
        String hash = PasswordHasher.hash(password);

        assertNotNull(hash);
        assertTrue(PasswordHasher.verify(password, hash));
        assertFalse(PasswordHasher.verify("wrongPassword", hash));
    }

    @Test
    void testDifferentHashesForSamePassword() {
        String password = "samePassword";
        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);

        // Should be different due to random salt
        assertNotEquals(hash1, hash2);

        // But both should verify correctly
        assertTrue(PasswordHasher.verify(password, hash1));
        assertTrue(PasswordHasher.verify(password, hash2));
    }
}