package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST,
    code = HttpStatus.BAD_REQUEST,
    reason = "AUTHORISE_CONSENT"
)
@Getter
public class MissingConsentAuthorisationException extends ParametrizedMessageException {

    private String consentId;
    private String authorisationId;
    private UpdateAuthResponse response;

    public MissingConsentAuthorisationException(UpdateAuthResponse response, String consentId, String authorisationId) {
        super("Solve Selected SCA Method");
        this.consentId = consentId;
        this.authorisationId = authorisationId;
        this.response = response;
    }
}
