package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static de.adorsys.multibanking.domain.ScaApproach.EMBEDDED;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST,
    code = HttpStatus.BAD_REQUEST,
    reason = "AUTHORISE_CONSENT"
)
@Getter
@EqualsAndHashCode(callSuper = false)
public class TransactionAuthorisationRequiredException extends ParametrizedMessageException {

    private final UpdateAuthResponse response;
    private final String consentId;
    private final String authorisationId;

    public TransactionAuthorisationRequiredException(UpdateAuthResponse updateAuthResponse, String consentId,
                                                     String authorisationId) {
        super(updateAuthResponse.getScaApproach() == EMBEDDED
            ? "Invalid consent status!"
            : "Consent not yet authorised!");
        addParam("scaApproach", updateAuthResponse.getScaApproach().toString());
        addParam("scaStatus", updateAuthResponse.getScaStatus().toString());
        this.consentId = consentId;
        this.authorisationId = authorisationId;
        this.response = updateAuthResponse;
    }
}
