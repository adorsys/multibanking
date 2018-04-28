package de.adorsys.multibanking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.adorsys.multibanking.exception.base.ParametrizedMessageException;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        reason = "INVALID_CATEGORIES"
)
public class InvalidCategoriesException extends ParametrizedMessageException {

    public InvalidCategoriesException(String message) {
        super("unable import categories");
        this.addParam("message", message);
    }

}
