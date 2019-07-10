package de.adorsys.multibanking.exception.domain;


import de.adorsys.multibanking.exception.ParametrizedMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        code = HttpStatus.BAD_REQUEST,
        reason = "MISSING_PIN"
)
public class MissingPinException extends ParametrizedMessageException {

    public MissingPinException() {
        super("Missing PIN for payment");
    }

}
