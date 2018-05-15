package de.adorsys.multibanking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.adorsys.multibanking.exception.base.ParametrizedMessageException;

import java.text.MessageFormat;

@ResponseStatus(
        value = HttpStatus.FORBIDDEN,
        reason = "INVALID_PIN"
)
public class InvalidPinException extends ParametrizedMessageException {

    public InvalidPinException(String accessId) {
        super(MessageFormat.format("invalid pin for bank access [{0}]", new Object[]{accessId}));
        this.addParam("account", accessId);
    }

}
