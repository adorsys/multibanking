package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.banking_gateway_b2c.ApiException;
import de.adorsys.multibanking.banking_gateway_b2c.api.BankingGatewayB2CAisApi;
import de.adorsys.multibanking.banking_gateway_b2c.model.*;
import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.ScaApproach;
import de.adorsys.multibanking.domain.ScaStatus;
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
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
import de.adorsys.multibanking.xs2a_adapter.model.Error400NGAIS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.Iban;

import java.util.Optional;

import static de.adorsys.multibanking.bg.ApiClientFactory.bankingGatewayB2CAisApi;
import static de.adorsys.multibanking.domain.BankApi.XS2A;
import static de.adorsys.multibanking.domain.exception.MultibankingError.*;

@RequiredArgsConstructor
@Slf4j
public class BankingGatewayScaHandler implements StrongCustomerAuthorisable {

    private final String bankingGatewayBaseUrl;
    private BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();

    @Override
    public CreateConsentResponse createConsent(Consent consentTemplate, boolean redirectPreferred,
                                               String tppRedirectUri, Object bankApiConsentData) {
        try {
            String bankCode = Iban.valueOf(consentTemplate.getPsuAccountIban()).getBankCode();
            BankingGatewayB2CAisApi aisApi = bankingGatewayB2CAisApi(bankingGatewayBaseUrl,
                (BgSessionData) bankApiConsentData);
            CreateConsentResponseTO consentResponse =
                aisApi.createConsentUsingPOST(bankingGatewayMapper.toConsentTO(consentTemplate), bankCode, null, null
                    , redirectPreferred, tppRedirectUri);

            BgSessionData sessionData = new BgSessionData();
            sessionData.setConsentId(consentResponse.getConsentId());
            sessionData.setBankCode(bankCode);
            Optional.ofNullable(bankApiConsentData).map(BgSessionData.class::cast).ifPresent(consentData -> {
                sessionData.setAccessToken(consentData.getAccessToken());
                sessionData.setRefreshToken(consentData.getRefreshToken());
            });
            CreateConsentResponse createConsentResponse =
                bankingGatewayMapper.toCreateConsentResponse(consentResponse);
            createConsentResponse.setBankApiConsentData(sessionData);

            return createConsentResponse;
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public Consent getConsent(String consentId) {
        try {
            return bankingGatewayMapper.toConsent(bankingGatewayB2CAisApi(bankingGatewayBaseUrl, null).getConsentUsingGET(consentId)); //
            // TODO Bearer token
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication) {
        try {
            UpdatePsuAuthenticationRequestTO updatePsuAuthenticationRequestTO =
                bankingGatewayMapper.toUpdatePsuAuthenticationRequestTO(updatePsuAuthentication.getCredentials());

            BankingGatewayB2CAisApi bankingGatewayB2CAisApi = bankingGatewayB2CAisApi(bankingGatewayBaseUrl, null);

            ResourceOfUpdateAuthResponseTO resourceUpdateAuthResponse =
                bankingGatewayB2CAisApi.updatePsuAuthenticationUsingPUT(updatePsuAuthenticationRequestTO
                    , updatePsuAuthentication.getAuthorisationId(), updatePsuAuthentication.getConsentId());

            return bankingGatewayMapper.toUpdateAuthResponse(resourceUpdateAuthResponse,
                new UpdateAuthResponse(XS2A,
                    bankingGatewayMapper.toScaApproach(resourceUpdateAuthResponse.getScaApproach()),
                    bankingGatewayMapper.toScaStatus(resourceUpdateAuthResponse.getScaStatus())));
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod) {
        try {
            BankingGatewayB2CAisApi bankingGatewayB2CAisApi = bankingGatewayB2CAisApi(bankingGatewayBaseUrl, null);

            ResourceOfUpdateAuthResponseTO resourceUpdateAuthResponse =
                bankingGatewayB2CAisApi.selectPsuAuthenticationMethodUsingPUT(bankingGatewayMapper.toSelectPsuAuthenticationMethodRequestTO(selectPsuAuthenticationMethod), selectPsuAuthenticationMethod.getAuthorisationId(), selectPsuAuthenticationMethod.getConsentId());

            return bankingGatewayMapper.toUpdateAuthResponse(resourceUpdateAuthResponse,
                new UpdateAuthResponse(XS2A,
                    ScaApproach.valueOf(resourceUpdateAuthResponse.getScaApproach().toString()),
                    ScaStatus.valueOf(resourceUpdateAuthResponse.getScaStatus().toString())));
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisationRequest) {
        try {
            BankingGatewayB2CAisApi bankingGatewayB2CAisApi = bankingGatewayB2CAisApi(bankingGatewayBaseUrl,
                (BgSessionData) transactionAuthorisationRequest.getBankApiConsentData());

            TransactionAuthorisationRequestTO transactionAuthorisationRequestTO =
                bankingGatewayMapper.toTransactionAuthorisationRequestTO(transactionAuthorisationRequest);

            ResourceOfUpdateAuthResponseTO resourceUpdateAuthResponse =
                bankingGatewayB2CAisApi.transactionAuthorisationUsingPUT(transactionAuthorisationRequestTO,
                    transactionAuthorisationRequest.getAuthorisationId(),
                    transactionAuthorisationRequest.getConsentId());

            return bankingGatewayMapper.toUpdateAuthResponse(resourceUpdateAuthResponse,
                new UpdateAuthResponse(XS2A,
                    ScaApproach.valueOf(resourceUpdateAuthResponse.getScaApproach().toString()),
                    ScaStatus.valueOf(resourceUpdateAuthResponse.getScaStatus().toString())));
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId,
                                                     Object bankApiConsentData) {
        try {
            BankingGatewayB2CAisApi bankingGatewayB2CAisApi = bankingGatewayB2CAisApi(bankingGatewayBaseUrl,
                (BgSessionData) bankApiConsentData);
            ResourceOfUpdateAuthResponseTO resourceUpdateAuthResponse =
                bankingGatewayB2CAisApi.getConsentAuthorisationStatusUsingGET(authorisationId,
                    consentId);

            return bankingGatewayMapper.toUpdateAuthResponse(resourceUpdateAuthResponse,
                new UpdateAuthResponse(XS2A,
                    ScaApproach.valueOf(resourceUpdateAuthResponse.getScaApproach().toString()),
                    ScaStatus.valueOf(resourceUpdateAuthResponse.getScaStatus().toString())));
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public void revokeConsent(String consentId) {
        try {
            BankingGatewayB2CAisApi bankingGatewayB2CAisApi = bankingGatewayB2CAisApi(bankingGatewayBaseUrl, null);
            bankingGatewayB2CAisApi.revokeConsentUsingDELETE(consentId); // TODO Bearer token
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public void validateConsent(String consentId, String authorisationId, ScaStatus expectedConsentStatus,
                                Object bankApiConsentData) {
        try {
            BankingGatewayB2CAisApi bankingGatewayB2CAisApi = bankingGatewayB2CAisApi(bankingGatewayBaseUrl,
                (BgSessionData) bankApiConsentData);

            Optional.of(bankingGatewayB2CAisApi.getConsentAuthorisationStatusUsingGET(authorisationId, consentId))
                .map(consentStatus -> ScaStatus.valueOf(consentStatus.getScaStatus().getValue()))
                .filter(consentStatus -> consentStatus == expectedConsentStatus)
                .orElseThrow(() -> new MultibankingException(MultibankingError.INVALID_CONSENT_STATUS));
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public void afterExecute(Object bankApiConsentData, AuthorisationCodeResponse authorisationCodeResponse) {
        //noop
    }

    @Override
    public void submitAuthorisationCode(Object bankApiConsentData, String authorisationCode) {
        BgSessionData sessionData = (BgSessionData) bankApiConsentData;
        String consentId = sessionData.getConsentId();

        try {
            AuthorizationCodeTO authorizationCodeTO = new AuthorizationCodeTO();
            authorizationCodeTO.setCode(authorisationCode);
            authorizationCodeTO.setBankCode(sessionData.getBankCode());

            BankingGatewayB2CAisApi bankingGatewayB2CAisApi = bankingGatewayB2CAisApi(bankingGatewayBaseUrl,
                sessionData);
            OAuthToken token = bankingGatewayB2CAisApi.resolveAuthCodeUsingPOST(authorizationCodeTO, consentId);

            Optional.ofNullable(token)
                .map(OAuthToken::getAccessToken)
                .orElseThrow(() -> new MultibankingException(INTERNAL_ERROR, 500, "No bearer token received " +
                    "for auth code"));

            sessionData.setAccessToken(token.getAccessToken());
            sessionData.setRefreshToken(token.getRefreshToken());
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public PaymentStatusResponse getPaymentStatus(TransactionRequest<PaymentStatusReqest> request) {
        return null;
    }

    private MultibankingException handeAisApiException(ApiException e) {
        switch (e.getCode()) {
            case 401:
                return toMultibankingException(e, INVALID_PIN);
            case 404:
                return toMultibankingException(e, RESOURCE_NOT_FOUND);
            case 429:
                return new MultibankingException(INVALID_CONSENT, 429, "consent access exceeded");
            default:
                return toMultibankingException(e, BANKING_GATEWAY_ERROR);
        }
    }

    private MultibankingException toMultibankingException(ApiException e, MultibankingError multibankingError) {
        try {
            Error400NGAIS messagesTO = ObjectMapperConfig.getObjectMapper().readValue(e.getResponseBody(),
                Error400NGAIS.class);
            return new MultibankingException(multibankingError, e.getCode(),
                bankingGatewayMapper.toMessages(messagesTO.getTppMessages()));
        } catch (Exception e2) {
            return new MultibankingException(BANKING_GATEWAY_ERROR, 500, e.getMessage());
        }
    }

}
