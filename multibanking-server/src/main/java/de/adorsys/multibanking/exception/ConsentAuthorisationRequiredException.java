package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.domain.Consent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConsentAuthorisationRequiredException extends ParametrizedMessageException {

    private final Consent consent;

    public ConsentAuthorisationRequiredException(Consent consent) {
        super("Consent authorisation required");
        this.consent = consent;
    }

}
