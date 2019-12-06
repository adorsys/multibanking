package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.transaction.*;
import de.adorsys.multibanking.mapper.TransactionsParser;
import de.adorsys.xs2a.adapter.mapper.TransactionsReportMapper;
import de.adorsys.xs2a.adapter.mapper.TransactionsReportMapperImpl;
import de.adorsys.xs2a.adapter.model.BookingStatusTO;
import de.adorsys.xs2a.adapter.model.PaymentProductTO;
import de.adorsys.xs2a.adapter.model.PaymentServiceTO;
import de.adorsys.xs2a.adapter.model.TransactionsResponse200JsonTO;
import de.adorsys.xs2a.adapter.remote.api.AccountApi;
import de.adorsys.xs2a.adapter.remote.api.AccountInformationClient;
import de.adorsys.xs2a.adapter.remote.service.impl.RemoteAccountInformationService;
import de.adorsys.xs2a.adapter.service.AccountInformationService;
import de.adorsys.xs2a.adapter.service.RequestHeaders;
import de.adorsys.xs2a.adapter.service.RequestParams;
import de.adorsys.xs2a.adapter.service.Response;
import de.adorsys.xs2a.adapter.service.model.AccountDetails;
import de.adorsys.xs2a.adapter.service.model.AccountListHolder;
import de.adorsys.xs2a.adapter.service.model.AccountReport;
import de.adorsys.xs2a.adapter.service.model.TransactionsReport;
import feign.Feign;
import feign.FeignException;
import feign.Logger;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

import static de.adorsys.multibanking.domain.BankApi.XS2A;
import static de.adorsys.multibanking.domain.exception.MultibankingError.*;

@Slf4j
public class BankingGatewayAdapter implements OnlineBankingService {

    private final BankingGatewayScaHandler scaHandler;
    @NonNull
    private final String xs2aAdapterBaseUrl;
    @Getter(lazy = true)
    private final AccountInformationClient accountApi = Feign.builder()
        .requestInterceptor(new FeignCorrelationIdInterceptor())
        .contract(createSpringMvcContract())
        .logLevel(Logger.Level.FULL)
        .logger(new Slf4jLogger(AccountApi.class))
        .encoder(new JacksonEncoder())
        .decoder(new ResponseEntityDecoder(new StringDecoder(new JacksonDecoder())))
        .target(AccountInformationClient.class, xs2aAdapterBaseUrl);
    @Getter(lazy = true)
    private final AccountInformationService accountInformationService =
        new RemoteAccountInformationService(getAccountApi());

    private BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();
    private TransactionsReportMapper transactionsReportMapper = new TransactionsReportMapperImpl();

    public BankingGatewayAdapter(String bankingGatewayBaseUrl, String xs2aAdapterBaseUrl) {
        this.scaHandler = new BankingGatewayScaHandler(bankingGatewayBaseUrl);
        this.xs2aAdapterBaseUrl = xs2aAdapterBaseUrl;
    }

    private SpringMvcContract createSpringMvcContract() {
        return new SpringMvcContract(Collections.emptyList(), new CustomConversionService());
    }

    @Override
    public BankApi bankApi() {
        return XS2A;
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
    public BankApiUser registerUser(String userId) {
        BankApiUser bankApiUser = new BankApiUser();
        bankApiUser.setBankApi(bankApi());
        return bankApiUser;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
        //noop
    }

    @Override
    public AccountInformationResponse loadBankAccounts(TransactionRequest<LoadAccounts> loadAccountInformationRequest) {
        String token =
            Optional.ofNullable(loadAccountInformationRequest.getBankApiConsentData()).map(BgSessionData.class::cast).map(BgSessionData::getAccessToken).orElse(null);
        RequestHeaders aisHeaders = createAisHeaders(loadAccountInformationRequest, token);

        Response<AccountListHolder> accountList = getAccountInformationService().getAccountList(aisHeaders,
            RequestParams.builder().build());

        List<BankAccount> bankAccounts = bankingGatewayMapper.toBankAccounts(accountList.getBody().getAccounts());

        return AccountInformationResponse.builder()
            .bankAccess(loadAccountInformationRequest.getBankAccess())
            .bankAccounts(bankAccounts)
            .build();
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //noop
    }

    public TransactionsResponse loadTransactions(TransactionRequest<LoadTransactions> loadTransactionsRequest) {
        LoadTransactions loadBookings = loadTransactionsRequest.getTransaction();
        String token =
            Optional.ofNullable(loadTransactionsRequest.getBankApiConsentData()).map(BgSessionData.class::cast).map(BgSessionData::getAccessToken).orElse(null);

        String resourceId = Optional.ofNullable(loadBookings.getPsuAccount().getExternalIdMap().get(bankApi()))
            .orElseGet(() -> getAccountResourceId(loadTransactionsRequest.getBankAccess().getIban(),
                createAisHeaders(loadTransactionsRequest, token)));

        RequestParams requestParams = RequestParams.builder()
            .dateFrom(loadBookings.getDateFrom() != null ? loadBookings.getDateFrom() : LocalDate.now().minusYears(1))
            .dateTo(loadBookings.getDateTo())
            .withBalance(loadBookings.isWithBalance())
            .bookingStatus(BookingStatusTO.BOOKED.toString()).build();

        try {
            RequestHeaders requestHeaders = createAisHeaders(loadTransactionsRequest, token);
            Response<String> transactionListString =
                getAccountInformationService().getTransactionListAsString(resourceId, requestHeaders, requestParams);
            Map<String, String> headersMap = transactionListString.getHeaders().getHeadersMap();
            String contentType = headersMap.keySet().stream()
                .filter(header -> header.toLowerCase().contains("content-type"))
                .map(headersMap::get)
                .findFirst()
                .orElse("");
            String body = transactionListString.getBody();

            if (contentType.toLowerCase().contains("application/xml")) {
                return TransactionsParser.camtStringToLoadBookingsResponse(body);
            } else if (contentType.toLowerCase().contains("text/plain")) {
                return TransactionsParser.mt940StringToLoadBookingsResponse(body);
            } else {
                return jsonStringToLoadBookingsResponse(body);
            }
        } catch (FeignException e) {
            throw handeAisApiException(e);
        } catch (Exception e) {
            throw new MultibankingException(INTERNAL_ERROR, 500, "Error loading bookings: " + e.getMessage());
        }
    }

    @Override
    public StandingOrdersResponse loadStandingOrders(TransactionRequest<LoadStandingOrders> loadStandingOrdersRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LoadBalancesResponse loadBalances(TransactionRequest<LoadBalances> request) {
        throw new UnsupportedOperationException();
    }

    private TransactionsResponse jsonStringToLoadBookingsResponse(String json) throws IOException {
        TransactionsResponse200JsonTO transactionsResponse200JsonTO =
            ObjectMapperConfig.getObjectMapper().readValue(json, TransactionsResponse200JsonTO.class);
        TransactionsReport transactionList =
            transactionsReportMapper.toTransactionsReport(transactionsResponse200JsonTO);
        List<Booking> bookings = Optional.ofNullable(transactionList)
            .map(TransactionsReport::getTransactions)
            .map(AccountReport::getBooked)
            .map(transactions -> bankingGatewayMapper.toBookings(transactions))
            .orElse(Collections.emptyList());

        BalancesReport balancesReport = new BalancesReport();
        Optional.ofNullable(transactionList)
            .map(TransactionsReport::getBalances)
            .orElse(Collections.emptyList())
            .forEach(balance -> {
                switch (balance.getBalanceType()) {
                    case EXPECTED:
                        balancesReport.setUnreadyBalance(bankingGatewayMapper.toBalance(balance));
                        break;
                    case CLOSINGBOOKED:
                        balancesReport.setReadyBalance(bankingGatewayMapper.toBalance(balance));
                        break;
                    default:
                        // ignore
                        break;
                }
            });

        return TransactionsResponse.builder()
            .bookings(bookings)
            .balancesReport(balancesReport)
            .build();
    }

    private String getAccountResourceId(String iban, RequestHeaders requestHeaders) {
        return getAccountInformationService().getAccountList(requestHeaders, RequestParams.builder().build())
            .getBody().getAccounts()
            .stream()
            .filter(accountDetails -> accountDetails.getIban().equals(iban))
            .findAny()
            .map(AccountDetails::getResourceId)
            .orElseThrow(() -> new MultibankingException(INVALID_ACCOUNT_REFERENCE));
    }

    private RequestHeaders createAisHeaders(TransactionRequest<?> transactionRequest, String bearerToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put(RequestHeaders.X_REQUEST_ID, UUID.randomUUID().toString());
        headers.put(RequestHeaders.CONSENT_ID, transactionRequest.getBankAccess().getConsentId());
        headers.put(RequestHeaders.X_GTW_BANK_CODE,
            transactionRequest.getBank().getBankApiBankCode() != null
                ? transactionRequest.getBank().getBankApiBankCode()
                : transactionRequest.getBankAccess().getBankCode());
        headers.put(RequestHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE); // TODO try camt
        Optional.ofNullable(bearerToken).ifPresent(token -> headers.put(RequestHeaders.AUTHORIZATION, String.format(
            "Bearer %s", token)));
        return RequestHeaders.fromMap(headers);
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
    public PaymentResponse executePayment(TransactionRequest<? extends AbstractPayment> paymentRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StrongCustomerAuthorisable getStrongCustomerAuthorisation() {
        return scaHandler;
    }

    private MultibankingException handeAisApiException(FeignException e) {
        if (e.status() == 429) {
            return new MultibankingException(CONSENT_ACCESS_EXCEEDED, 429, "consent access exceeded");
        }
        return new MultibankingException(BANKING_GATEWAY_ERROR, e.status(), e.getMessage());
    }

    public static class CustomConversionService extends DefaultConversionService {
        CustomConversionService() {
            addConverter(BookingStatusTO.class, String.class, BookingStatusTO::toString);
            addConverter(PaymentProductTO.class, String.class, PaymentProductTO::toString);
            addConverter(PaymentServiceTO.class, String.class, PaymentServiceTO::toString);
        }
    }

    @RequiredArgsConstructor
    public static class StringDecoder implements Decoder {
        @NonNull
        private final Decoder delegate;

        @Override
        public Object decode(feign.Response response, Type type) throws IOException {
            if (String.class.getName().equals(type.getTypeName())) {
                feign.Response.Body body = response.body();
                return IOUtils.toString(body.asInputStream());
            }
            return delegate.decode(response, type);
        }
    }
}

