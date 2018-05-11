package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.exception.domain.Message;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class SmartanalyticsException extends RuntimeException {

    private HttpStatus status;
    private Message errorMessage;

    public SmartanalyticsException(HttpStatus status, Message errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

}
