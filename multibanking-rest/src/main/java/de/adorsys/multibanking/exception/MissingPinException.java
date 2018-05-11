package de.adorsys.multibanking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.adorsys.multibanking.exception.base.ParametrizedMessageException;

import java.text.MessageFormat;

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
