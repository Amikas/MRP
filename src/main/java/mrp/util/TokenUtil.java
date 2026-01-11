package mrp.util;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

public class TokenUtil {

    public static String generateToken() {
        return UuidCreator.getTimeOrdered().toString();
    }

    public static boolean isValidToken(String token) {
        if (token == null) {
            return false;
        }
        try {
            UUID.fromString(token);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}