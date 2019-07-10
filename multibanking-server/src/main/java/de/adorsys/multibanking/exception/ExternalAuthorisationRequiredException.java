package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.domain.Consent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST,
    code = HttpStatus.BAD_REQUEST,
    reason = "AUTHORISE_CONSENT"
)
@Data
@EqualsAndHashCode(callSuper = false)
public class ExternalAuthorisationRequiredException extends ParametrizedMessageException {

    private final Consent consent;

    public ExternalAuthorisationRequiredException(Consent consent) {
        super("Consent authorisation required");
        this.addParam("authUrl", consent.getAuthUrl());
        this.consent = consent;
    }

}
