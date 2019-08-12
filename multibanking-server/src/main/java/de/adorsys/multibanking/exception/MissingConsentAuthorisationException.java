package de.adorsys.multibanking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST,
    code = HttpStatus.BAD_REQUEST,
    reason = "AUTHORISE_CONSENT"
)
public class MissingConsentAuthorisationException extends ParametrizedMessageException {
    public MissingConsentAuthorisationException() {
        super("Solve Selected SCA Method");
    }
}
