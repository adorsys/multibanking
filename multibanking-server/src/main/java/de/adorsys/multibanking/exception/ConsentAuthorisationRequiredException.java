package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.domain.Consent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Optional;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST,
    code = HttpStatus.BAD_REQUEST,
    reason = "AUTHORISE_CONSENT"
)

@Data
@EqualsAndHashCode(callSuper = false)
public class ConsentAuthorisationRequiredException extends ParametrizedMessageException {

    private final Consent consent;

    public ConsentAuthorisationRequiredException(Consent consent, String authUrl) {
        super("Consent authorisation required");

        Optional.ofNullable(authUrl)
            .ifPresent(url -> addParam("authUrl", url));

        Optional.ofNullable(consent.getRedirectUrl())
            .ifPresent(url -> addParam("redirectUrl", url));

        this.consent = consent;
    }

}
