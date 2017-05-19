package utils;

import java.security.SecureRandom;

/**
 * Created by alexg on 18.05.17.
 */
public final class Utils {

    public static SecureRandom getSecureRandom() {
        try {
            SecureRandom random = new SecureRandom();
            byte seed[] = random.generateSeed(20);
            random.setSeed(seed);
            return random;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
