package de.adorsys.multibanking.ing;

import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.ConsentStatus;
import de.adorsys.multibanking.domain.ScaStatus;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.PaymentStatusResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.transaction.PaymentStatusReqest;
import lombok.RequiredArgsConstructor;

import static de.adorsys.multibanking.domain.BankApi.ING;
import static de.adorsys.multibanking.domain.ScaApproach.OAUTH;
import static de.adorsys.multibanking.domain.ScaStatus.STARTED;

@RequiredArgsConstructor
public class IngScaHandler implements StrongCustomerAuthorisable {

    private final IngSessionHandler ingSessionHandler;
    private IngMapper ingMapper = new IngMapperImpl();

    @Override
    public CreateConsentResponse createConsent(Consent consentTemplate, boolean redirectPreferred,
                                               String tppRedirectUri, Object bankApiConsentData) {
        IngSessionData ingSessionData = new IngSessionData();
        ingSessionData.setStatus(STARTED);
        ingSessionData.setTppRedirectUri(tppRedirectUri);

        return ingMapper.toCreateConsentResponse(ingSessionData,
            ingSessionHandler.getAuthorisationUri(tppRedirectUri).toString());
    }

    @Override
    public Consent getConsent(String consentId, Object bankApiConsentData) {
        return null;
    }

    @Override
    public ConsentStatus getConsentStatus(String consentId, Object bankApiConsentData) {
        return null; // TODO
    }

    @Override
    public UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisationRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revokeConsent(String consentId, Object bankApiConsentData) {
        //noop
    }

    @Override
    public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId,
                                                     Object bankApiConsentData) {
        IngSessionData ingSessionData = (IngSessionData) bankApiConsentData;
        return ingMapper.toUpdateAuthResponse(ingSessionData, new UpdateAuthResponse(ING, OAUTH,
            ingSessionData.getStatus()));
    }

    @Override
    public void validateConsent(String consentId, String authorisationId, ScaStatus expectedConsentStatus,
                                Object bankApiConsentData) {
        //noop
    }

    @Override
    public void afterExecute(Object bankApiConsentData, AuthorisationCodeResponse authorisationCodeResponse) {
        //noop
    }

    @Override
    public void submitAuthorisationCode(Object bankApiConsentData, String authorisationCode) {
        IngSessionData ingSessionData = (IngSessionData) bankApiConsentData;
        ingSessionHandler.checkIngSession(ingSessionData, authorisationCode);
    }

    @Override
    public PaymentStatusResponse getPaymentStatus(TransactionRequest<PaymentStatusReqest> request) {
        throw new UnsupportedOperationException();
    }
}
