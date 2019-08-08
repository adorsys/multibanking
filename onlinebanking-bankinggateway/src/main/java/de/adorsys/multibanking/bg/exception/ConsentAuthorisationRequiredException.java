package de.adorsys.multibanking.bg.exception;

import de.adorsys.multibanking.domain.spi.Consent;
import de.adorsys.multibanking.domain.exception.MissingAuthorisationException;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConsentAuthorisationRequiredException extends MissingAuthorisationException {

    public ConsentAuthorisationRequiredException(Consent consent) {
        super(consent, "Consent authorisation required");
    }

}
