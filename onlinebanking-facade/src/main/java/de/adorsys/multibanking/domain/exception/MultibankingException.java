package de.adorsys.multibanking.domain.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MultibankingException extends RuntimeException {

    private MultibankingError multibankingError;

    public MultibankingException(MultibankingError multibankingError) {
        this(multibankingError, null);
    }

    public MultibankingException(MultibankingError multibankingError, String message) {
        super(message);
        this.multibankingError = multibankingError;
    }

    public MultibankingException(Throwable cause) {
        super(cause);
    }

}
