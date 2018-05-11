package de.adorsys.multibanking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.adorsys.multibanking.exception.base.ParametrizedMessageException;

@ResponseStatus(
        value = HttpStatus.PRECONDITION_FAILED,
        code = HttpStatus.PRECONDITION_FAILED,
        reason = UserNotFoundException.MESSAGE_KEY
)
public class UserNotFoundException extends ParametrizedMessageException {
	private static final long serialVersionUID = -5652582388782143286L;
	public static final String MESSAGE_KEY = "user.not.found";
	public static final String RENDERED_MESSAGE_KEY = "User with Id [{1}] not found. First create the user.";
	public static final String MESSAGE_DOC = MESSAGE_KEY + ": User with provided id does not exist. First create the user.";
	
    public UserNotFoundException(String userId) {
        super(String.format(RENDERED_MESSAGE_KEY, userId));
        this.addParam("userId", userId);
    }
}
