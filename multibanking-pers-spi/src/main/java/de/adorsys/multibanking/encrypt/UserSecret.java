package de.adorsys.multibanking.encrypt;

/**
 * Holds the user custom secret. This will be injected by the security implementation.
 *
 * @author fpo
 */
public class UserSecret {

    public String secret;

    public UserSecret(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }

}
