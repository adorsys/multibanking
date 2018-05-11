package de.adorsys.multibanking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.adorsys.multibanking.exception.base.ParametrizedMessageException;

import java.text.MessageFormat;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        reason = "ERROR_PAYMENT"
)
public class PaymentException extends ParametrizedMessageException {

    public PaymentException(String msg) {
        super();
        this.addParam("msg", msg);
    }

}
