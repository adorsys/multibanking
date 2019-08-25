package de.adorsys.multibanking.domain.spi;

import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.ScaStatus;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;

public interface StrongCustomerAuthorisable {

    CreateConsentResponse createConsent(Consent consentTemplate, boolean redirectPreferred, String tppRedirectUri);

    Consent getConsent(String consentId);

    UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication);

    UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisation);

    UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod);

    void revokeConsent(String consentId);

    UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId, Object bankApiConsentData);

    /**
     * @param consentId
     * @param authorisationId
     * @param expectedConsentStatus
     * @param bankApiConsentData
     * @throws MultibankingException INVALID_PIN for Consent without login
     *                               INVALID_SCA_METHOD for Consent without selected sca method
     *                               HBCI_2FA_REQUIRED for Consent without authorized sca method
     */
    void validateConsent(String consentId, String authorisationId, ScaStatus expectedConsentStatus,
                         Object bankApiConsentData);

    void afterExecute(Object bankApiConsentData, AuthorisationCodeResponse authorisationCodeResponse);
}
