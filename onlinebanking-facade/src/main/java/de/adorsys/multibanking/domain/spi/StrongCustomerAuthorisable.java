package de.adorsys.multibanking.domain.spi;

import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.exception.MultibankingException;

public interface StrongCustomerAuthorisable {
    Consent createConsent(Consent consentTemplate);
    Consent getConsent(String consentId);
    Consent loginConsent(Consent consent);
    Consent authorizeConsent(Consent consent);
    Consent selectScaMethod(Consent consent);

    /**
     *
     * @param consentId
     * @throws MultibankingException
     *      INVALID_PIN for Consent without login
     *      INVALID_SCA_METHOD for Consent without selected sca method
     *      INVALID_TAN for Consent without authorized sca method
     */
    void validateConsent(String consentId) throws MultibankingException;
}
