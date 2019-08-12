package de.adorsys.multibanking.domain.spi;

import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.ScaStatus;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;

public interface StrongCustomerAuthorisable {

    CreateConsentResponse createConsent(Consent consentTemplate);

    Consent getConsent(String consentId);

    UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication, String bankUrl);

    UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisation);

    UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod);

    void revokeConsent(String consentId);

    UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId);

    /**
     * @param consentId
     * @throws MultibankingException INVALID_PIN for Consent without login
     *                               INVALID_SCA_METHOD for Consent without selected sca method
     *                               INVALID_TAN for Consent without authorized sca method
     */
    void validateConsent(String consentId, ScaStatus consentStatus) throws MultibankingException;
}
