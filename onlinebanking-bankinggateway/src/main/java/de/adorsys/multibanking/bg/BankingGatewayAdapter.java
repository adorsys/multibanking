package de.adorsys.multibanking.bg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.adorsys.multibanking.bg.api.AccountInformationServiceAisApi;
import de.adorsys.multibanking.bg.api.PaymentInitiationServicePisApi;
import de.adorsys.multibanking.bg.model.*;
import de.adorsys.multibanking.bg.pis.PaymentInitiationBuilderStrategy;
import de.adorsys.multibanking.bg.pis.PaymentInitiationBuilderStrategyImpl;
import de.adorsys.multibanking.bg.pis.PaymentProductType;
import de.adorsys.multibanking.bg.pis.PaymentServiceType;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.ConsentStatus;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.*;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.bg.BankingGatewayMapping.toConsents;
import static de.adorsys.multibanking.bg.model.MessageCode400AIS.CONSENT_UNKNOWN;
import static de.adorsys.multibanking.bg.model.MessageCode401AIS.CONSENT_INVALID;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_CONSENT;

public class BankingGatewayAdapter implements OnlineBankingService {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String bgCertificate;
    private PaymentInitiationBuilderStrategy initiationBuilderStrategy = new PaymentInitiationBuilderStrategyImpl();
    private ObjectMapper objectMapper = new ObjectMapper();

    public BankingGatewayAdapter() {
        bgCertificate = Optional.ofNullable(System.getenv("bg-cert"))
            .map(this::readBankingGatewayCertficate)
            .orElseGet(() -> {
                logger.warn("Missing env property bg-cert");
                return null;
            });
    }

    @Override
    public BankApi bankApi() {
        return BankApi.BANKING_GATEWAY;
    }

    @Override
    public boolean externalBankAccountRequired() {
        return false;
    }

    @Override
    public boolean userRegistrationRequired() {
        return true;
    }

    @Override
    public BankApiUser registerUser(BankAccess bankAccess, String pin) {
        BankApiUser bankApiUser = new BankApiUser();
        bankApiUser.setBankApi(bankApi());
        return bankApiUser;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(String bankingUrl,
                                                           LoadAccountInformationRequest loadAccountInformationRequest) {
        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(apiClient(bankingUrl));

        try {
            AccountList accountList = ais.getAccountList(
                loadAccountInformationRequest.getBankAccess().getBankCode(),
                UUID.randomUUID(),
                loadAccountInformationRequest.getConsentId(), false,
                null, null, null,
                null, PSU_IP_ADDRESS, null, null,
                null, null,
                null, null, null, null);

            return LoadAccountInformationResponse.builder()
                .bankAccounts(accountList.getAccounts()
                    .stream()
                    .map(BankingGatewayMapping::toBankAccount)
                    .collect(Collectors.toList()))
                .build();
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //noop
    }

    @Override
    public LoadBookingsResponse loadBookings(String bankingUrl, LoadBookingsRequest loadBookingsRequest) {
        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(apiClient(bankingUrl));
        String resourceId = loadBookingsRequest.getBankAccount().getExternalIdMap().get(BankApi.XS2A);
        LocalDate dateFrom = loadBookingsRequest.getDateFrom() != null ? loadBookingsRequest.getDateFrom() :
            LocalDate.now().minusYears(1);
        LocalDate dateTo = loadBookingsRequest.getDateTo();
        try {
            TransactionsResponse200Json transactionList = ais.getTransactionList(
                loadBookingsRequest.getBankAccess().getBankCode(),
                resourceId, "booked", UUID.randomUUID(),
                loadBookingsRequest.getConsentId(), dateFrom, dateTo, null, null,
                null, null, null, null, null,
                PSU_IP_ADDRESS, null, null, null,
                null, null, null, null,
                null);

            return LoadBookingsResponse.builder()
                .bookings(BankingGatewayMapping.toBookings(transactionList))
                .build();

        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public boolean bankSupported(String bankCode) {
        return true;
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    @Override
    public InitiatePaymentResponse initiatePayment(String bankingUrl, TransactionRequest paymentRequest) {
        UUID xRequestId = UUID.randomUUID();
        AbstractScaTransaction payment = paymentRequest.getTransaction();
        PaymentProductType paymentProduct = PaymentProductType.resolve(payment.getProduct());
        PaymentServiceType paymentService = PaymentServiceType.resolve(payment);
        String contentType = "application/" + (paymentProduct.isRaw() ? "xml" : "json");
        String psuId = paymentRequest.getBankAccess().getBankLogin();
        Object paymentBody = initiationBuilderStrategy.resolve(paymentProduct, paymentService).buildBody(payment);
        ApiClient apiClient = apiClient(bankingUrl, null, contentType);
        PaymentInitiationServicePisApi initiationService = new PaymentInitiationServicePisApi(apiClient);

        try {
            PaymentInitationRequestResponse201 response = initiationService.initiatePayment(
                paymentBody,
                paymentRequest.getBankAccess().getBankCode(),
                xRequestId,
                PSU_IP_ADDRESS,
                paymentService.getType(),
                paymentProduct.getType(),
                null, null, null, psuId, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null, null, null, null);

            return getInitiatePaymentResponse(response);

        } catch (ApiException e) {
            throw handePisApiException(e);
        }
    }

    private InitiatePaymentResponse getInitiatePaymentResponse(PaymentInitationRequestResponse201 response) {
        String transactionStatus = response.getTransactionStatus().toString();
        String paymentId = response.getPaymentId();
        String redirect = response.getLinks().get("scaRedirect").getHref();
        return new InitiatePaymentResponse(transactionStatus, paymentId, redirect);
    }

    @Override
    public AuthorisationCodeResponse requestAuthorizationCode(String bankingUrl, TransactionRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SubmitAuthorizationCodeResponse submitAuthorizationCode(SubmitAuthorizationCodeRequest submitPaymentRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean psd2Scope() {
        return true;
    }

    @Override
    public CreateConsentResponse createAccountInformationConsent(String bankingUrl,
                                                                 CreateConsentRequest createConsentRequest) {
        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(apiClient(bankingUrl));

        Consents consents = toConsents(createConsentRequest);
        BankAccess bankAccess = createConsentRequest.getBankAccess();

        ConsentsResponse201 response;
        try {
            response = ais.createConsent(
                bankAccess.getIban(), UUID.randomUUID(), consents, null, null, null, bankAccess.getBankLogin(), null,
                bankAccess.getBankLogin2(), null, "false", null, null, null, null, null, PSU_IP_ADDRESS, null, null,
                null, null, null, null, null, null, null);
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }

        // The consent object to be retrieved by the GET Consent Request will contain the adjusted date
        // NextGenPSD2 Access to Account Interoperability Framework - Implementation Guidelines V1.3_20181019.pdf
        // 6.4.1.1
        String consentId = response.getConsentId();
        ConsentInformationResponse200Json consentInformation;
        try {
            consentInformation = ais.getConsentInformation(bankAccess.getBankCode(), consentId, UUID.randomUUID(),
                null, null, null,
                PSU_IP_ADDRESS, null, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }

        return CreateConsentResponse.builder()
            .consentStatus(ConsentStatus.valueOf(consentInformation.getConsentStatus().getValue()))
            .consentId(consentId)
            .validUntil(consentInformation.getValidUntil())
            .scaRedirectUrl(response.getLinks().get("scaRedirect").getHref())
            .build();
    }

    private ApiClient apiClient(String url) {
        return apiClient(url, null, null);
    }

    private ApiClient apiClient(String url, String acceptHeader, String contentTypeHeader) {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(600, TimeUnit.SECONDS);
        client.interceptors().add(
            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        );

        ApiClient apiClient = new ApiClient() {
            @Override
            public String selectHeaderAccept(String[] accepts) {
                return Optional.ofNullable(acceptHeader)
                    .orElseGet(() -> super.selectHeaderAccept(accepts));
            }

            @Override
            public String selectHeaderContentType(String[] contentTypes) {
                return Optional.ofNullable(contentTypeHeader)
                    .orElseGet(() -> super.selectHeaderContentType(contentTypes));
            }

        };

        Optional.ofNullable(bgCertificate)
            .ifPresent(cert -> apiClient.addDefaultHeader("tpp-qwac-certificate", cert));
        apiClient.setHttpClient(client);
        apiClient.setBasePath(url);
        return apiClient;
    }

    private MultibankingException handeAisApiException(ApiException e) {
        try {
            switch (e.getCode()) {
                case 400:
                    return handleAis400Error(e);
                case 401:
                    return handleAis401Error(e);
                case 429:
                    return new MultibankingException(INVALID_CONSENT, "consent access exceeded");
                default:
                    throw new MultibankingException(e);
            }
        } catch (IOException ex) {
            logger.warn("unable to deserialize ApiException", ex);
        }

        throw new MultibankingException(e);
    }

    private MultibankingException handleAis401Error(ApiException e) throws IOException {
        for (TppMessage401AIS tppMessage :
            (objectMapper.readValue(e.getResponseBody(), Error401NGAIS.class)).getTppMessages()) {
            if (tppMessage.getCode() == CONSENT_INVALID) {
                return new MultibankingException(INVALID_CONSENT, tppMessage.getText());
            }
        }
        return new MultibankingException(e);
    }

    private MultibankingException handleAis400Error(ApiException e) throws IOException {
        for (TppMessage400AIS tppMessage :
            (objectMapper.readValue(e.getResponseBody(), Error400NGAIS.class)).getTppMessages()) {
            if (tppMessage.getCode() == CONSENT_UNKNOWN) {
                return new MultibankingException(INVALID_CONSENT, tppMessage.getText());
            }
        }
        return new MultibankingException(e);
    }

    private MultibankingException handePisApiException(ApiException e) {
        try {
            Error400NGPIS errorMessages = objectMapper.readValue(e.getResponseBody(), Error400NGPIS.class);
        } catch (IOException ex) {
            logger.warn("unable to deserialize ApiException", ex);
        }

        throw new MultibankingException(e);
    }

    private String readBankingGatewayCertficate(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Error loading bg certificate", e);
        }

    }
}

