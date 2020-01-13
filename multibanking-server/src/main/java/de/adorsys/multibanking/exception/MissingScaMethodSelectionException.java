package de.adorsys.multibanking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST,
    code = HttpStatus.BAD_REQUEST,
    reason = "SELECT_SCA_METHOD"
)
public class MissingScaMethodSelectionException extends ParametrizedMessageException {
    public MissingScaMethodSelectionException() {
        super("Select SCA Method");
    }
}
