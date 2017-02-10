package de.adorsys.multibanking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        reason = "INVALID_BANK_ACCESS"
)
public class InvalidBankAccessException extends ParametrizedMessageException {
    public InvalidBankAccessException(String bankCode, String bankUser) {
        super(MessageFormat.format("Bankzugang [{0}] mit Konto [{1}] nicht gefunden.", new Object[]{bankCode, bankUser}));
        this.addParam("bankCode", bankCode);
        this.addParam("bankLogin", bankUser);
    }
}
