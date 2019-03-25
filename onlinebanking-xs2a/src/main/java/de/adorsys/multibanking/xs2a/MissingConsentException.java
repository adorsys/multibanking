package de.adorsys.multibanking.xs2a;

public class MissingConsentException extends RuntimeException {
    private static final long serialVersionUID = 4655354574351100460L;

    public MissingConsentException(String message) {
        super(message);
    }
}
