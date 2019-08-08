package de.adorsys.multibanking.bg.exception;

import de.adorsys.multibanking.domain.exception.MissingAuthorisationException;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConsentRequiredException extends MissingAuthorisationException {

    public ConsentRequiredException() {
        super(null, "Consent required");
    }

}
