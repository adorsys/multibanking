package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.exception.domain.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = false)
public class SmartanalyticsException extends RuntimeException {

    private final HttpStatus status;
    private final Message errorMessage;

    public SmartanalyticsException(HttpStatus status, Message errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

}
