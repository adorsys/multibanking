package de.adorsys.multibanking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.adorsys.multibanking.exception.base.ParametrizedMessageException;

@ResponseStatus(
        value = HttpStatus.CONFLICT,
        code = HttpStatus.CONFLICT,
        reason = BankAccessAlreadyExistException.MESSAGE_KEY
)
public class BankAccessAlreadyExistException extends ParametrizedMessageException {
	private static final long serialVersionUID = 5078077955213908774L;
	public static final String MESSAGE_KEY = "bankaccess.already.exists";
	public static final String RENDERED_MESSAGE_KEY = "Bankaccess with Id [{1}] already exists.";
	public static final String MESSAGE_DOC = MESSAGE_KEY + ": A bank access with provided bank code already exists.";

    public BankAccessAlreadyExistException(String accessId) {
        super(String.format(RENDERED_MESSAGE_KEY, accessId));
        addParam("accessId", accessId);
    }
}
