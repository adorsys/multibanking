package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST,
    code = HttpStatus.BAD_REQUEST,
    reason = "NO_AUTHORISATION"
)
public class MissingStrongCustomerAuthorisationException extends ParametrizedMessageException {
    public MissingStrongCustomerAuthorisationException(String message) {
        super(message);
    }
}