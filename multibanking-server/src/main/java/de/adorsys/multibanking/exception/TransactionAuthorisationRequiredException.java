package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST,
    code = HttpStatus.BAD_REQUEST,
    reason = "AUTHORISE_CONSENT"
)
@Getter
@EqualsAndHashCode(callSuper = false)
public class TransactionAuthorisationRequiredException extends ParametrizedMessageException {

    private final String consentId;
    private final String authorisationId;
    private final UpdateAuthResponse response;

    public TransactionAuthorisationRequiredException(UpdateAuthResponse response, String consentId, String authorisationId) {
        super("Solve Selected SCA Method");
        this.consentId = consentId;
        this.authorisationId = authorisationId;
        this.response = response;
    }
}
