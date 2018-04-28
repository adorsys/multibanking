package de.adorsys.multibanking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.adorsys.multibanking.exception.base.ParametrizedMessageException;

@ResponseStatus(
        value = HttpStatus.FORBIDDEN,
        code = HttpStatus.FORBIDDEN,
        reason = InvalidBankAccessException.MESSAGE_KEY
)
public class InvalidBankAccessException extends ParametrizedMessageException {
	private static final long serialVersionUID = 6942393980381026635L;
	public static final String MESSAGE_KEY = "credential.invalid";
	public static final String RENDERED_MESSAGE_KEY = "Credentials not valid for bank code [{1}]";
	public static final String MESSAGE_DOC = MESSAGE_KEY + ": Credentials not valid for provided bank code";

    public InvalidBankAccessException(String bankCode) {
        super(String.format(RENDERED_MESSAGE_KEY, bankCode));
        this.addParam("bankCode", bankCode);
    }

}
