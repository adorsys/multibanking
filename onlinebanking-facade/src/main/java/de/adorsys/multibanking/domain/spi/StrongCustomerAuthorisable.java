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

    CreateConsentResponse createConsent(Consent consentTemplate, boolean redirectPreferred, String tppRedirectUri, Object bankApiConsentData);

    Consent getConsent(String consentId);

    UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication);

    UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisation);

    UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod);

    void revokeConsent(String consentId);

    UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId, Object bankApiConsentData);

    /**
     * @param consentId consent id
     * @param authorisationId authorisation id
     * @param expectedConsentStatus consent status
     * @param bankApiConsentData bank api specific consent data
     */
    void validateConsent(String consentId, String authorisationId, ScaStatus expectedConsentStatus,
                         Object bankApiConsentData);

    void afterExecute(Object bankApiConsentData, AuthorisationCodeResponse authorisationCodeResponse);

    void submitAuthorisationCode(Object bankApiConsentData, String authorisationCode);
}
