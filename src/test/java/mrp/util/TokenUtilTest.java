// File: test/util/TokenUtilTest.java
package mrp.util;

import org.junit.jupiter.api.*;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

public class TokenUtilTest {

    @Test
    void testGenerateToken() {
        String token = TokenUtil.generateToken();
        assertNotNull(token);
        assertTrue(token.length() > 30); // UUID should be at least 36 chars

        // Should be valid UUID
        assertTrue(TokenUtil.isValidToken(token));
    }

    @Test
    void testIsValidToken() {
        assertTrue(TokenUtil.isValidToken("12345678-1234-1234-1234-123456789abc"));
        assertFalse(TokenUtil.isValidToken("invalid-token"));
        assertFalse(TokenUtil.isValidToken(null));
        assertFalse(TokenUtil.isValidToken(""));
    }
}