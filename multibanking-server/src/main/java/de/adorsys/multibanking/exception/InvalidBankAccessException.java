package de.adorsys.multibanking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        code = HttpStatus.BAD_REQUEST,
        reason = "INVALID_BANK_ACCESS"
)
public class InvalidBankAccessException extends ParametrizedMessageException {

    public InvalidBankAccessException(String bankCode) {
        super(MessageFormat.format("Unsupported bank access code [{0}]", new Object[]{bankCode}));
        this.addParam("bankCode", bankCode);
    }

}
