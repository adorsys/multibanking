package de.adorsys.multibanking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        reason = "BANK_ACCESS_ALREADY_EXIST"
)
public class BankAccessAlreadyExistException extends ParametrizedMessageException {

    public BankAccessAlreadyExistException() {
        super("Bank access already exist");
    }

}
