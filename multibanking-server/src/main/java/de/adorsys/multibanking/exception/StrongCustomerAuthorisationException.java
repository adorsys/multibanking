package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST,
    code = HttpStatus.BAD_REQUEST,
    reason = "AUTHORISE_CONSENT"
)
public class StrongCustomerAuthorisationException extends ParametrizedMessageException {
    public StrongCustomerAuthorisationException(StrongCustomerAuthorisation authorisation, String message) {
        super(message);
        addParam("authinfo", authorisation.toExceptionInfo());
    }
}
