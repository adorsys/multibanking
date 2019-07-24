package de.adorsys.multibanking.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST,
    code = HttpStatus.BAD_REQUEST,
    reason = "NO_CONSENT"
)

@Data
@EqualsAndHashCode(callSuper = false)
public class ConsentRequiredException extends ParametrizedMessageException {

    public ConsentRequiredException() {
        super("Consent required");
    }

}
