package de.adorsys.multibanking.bg;

import com.squareup.okhttp.Call;
import de.adorsys.multibanking.bg.mapper.BankingGatewayExceptionMapper;
import de.adorsys.multibanking.bg.mapper.BankingGatewayMapper;
import de.adorsys.multibanking.bg.mapper.BankingGatewayMapperImpl;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.transaction.*;
import de.adorsys.multibanking.mapper.TransactionsParser;
import de.adorsys.multibanking.xs2a_adapter.ApiException;
import de.adorsys.multibanking.xs2a_adapter.ApiResponse;
import de.adorsys.multibanking.xs2a_adapter.api.AccountInformationServiceAisApi;
import de.adorsys.multibanking.xs2a_adapter.model.AccountDetails;
import de.adorsys.multibanking.xs2a_adapter.model.AccountList;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.multibanking.bg.ApiClientFactory.accountInformationServiceAisApi;
import static de.adorsys.multibanking.domain.BankApi.XS2A;
import static de.adorsys.multibanking.domain.exception.MultibankingError.*;

@Slf4j
public class BankingGatewayAdapter implements OnlineBankingService {

    private final BankingGatewayScaHandler scaHandler;
    private final String xs2aAdapterBaseUrl;
    private final PaginationResolver paginationResolver;

    private BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();

    public BankingGatewayAdapter(String bankingGatewayBaseUrl, String xs2aAdapterBaseUrl) {
        this.scaHandler = new BankingGatewayScaHandler(bankingGatewayBaseUrl);
        this.paginationResolver = new PaginationResolver(xs2aAdapterBaseUrl);
        this.xs2aAdapterBaseUrl = xs2aAdapterBaseUrl;
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
        try {
            String bankCode = loadAccountInformationRequest.getBank().getBankApiBankCode() != null
                ? loadAccountInformationRequest.getBank().getBankApiBankCode()
                : loadAccountInformationRequest.getBankAccess().getBankCode();
            BgSessionData bgSessionData = (BgSessionData) loadAccountInformationRequest.getBankApiConsentData();

            AccountList accountList = getAccountList(bgSessionData, bankCode,
                loadAccountInformationRequest.getBankAccess().getConsentId());

            List<BankAccount> bankAccounts = bankingGatewayMapper.toBankAccounts(accountList.getAccounts());

            return AccountInformationResponse.builder()
                .bankAccess(loadAccountInformationRequest.getBankAccess())
                .bankAccounts(bankAccounts)
                .build();
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    private AccountList getAccountList(BgSessionData bgSessionData, String bankCode, String consentId) throws ApiException {
        AccountInformationServiceAisApi aisApi = accountInformationServiceAisApi(xs2aAdapterBaseUrl, bgSessionData);

        return aisApi.getAccountList(UUID.randomUUID(), consentId, null, bankCode, null, false, null, null,
            null, null,
            null, null, null, null, null, null, null, null, null);
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //noop
    }

    public TransactionsResponse loadTransactions(TransactionRequest<LoadTransactions> loadTransactionsRequest) {
        LoadTransactions loadTransactions = loadTransactionsRequest.getTransaction();
        LocalDate dateFrom = loadTransactions.getDateFrom() != null ? loadTransactions.getDateFrom() :
            LocalDate.now().minusYears(1);
        LocalDate dateTo = loadTransactions.getDateTo();
        String consentId = loadTransactionsRequest.getBankAccess().getConsentId();
        boolean withBalance = loadTransactions.isWithBalance();

        String bankCode = loadTransactionsRequest.getBank().getBankApiBankCode() != null
            ? loadTransactionsRequest.getBank().getBankApiBankCode()
            : loadTransactionsRequest.getBankAccess().getBankCode();
        BgSessionData bgSessionData = (BgSessionData) loadTransactionsRequest.getBankApiConsentData();

        try {
            String resourceId = Optional.ofNullable(loadTransactions.getPsuAccount().getExternalIdMap().get(bankApi()))
                .orElseGet(() -> getAccountResourceId(bgSessionData,
                    loadTransactionsRequest.getBankAccess().getIban(), bankCode,
                    loadTransactionsRequest.getBankAccess().getConsentId()));

            AccountInformationServiceAisApi aisApi = accountInformationServiceAisApi(xs2aAdapterBaseUrl,
                bgSessionData);

            Call aisCall = aisApi.getTransactionListCall(
                resourceId, "booked", UUID.randomUUID(),
                consentId, null, bankCode, null, dateFrom,
                dateTo, null,
                null, withBalance, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);

            ApiResponse<Object> apiResponse = aisApi.getApiClient().execute(aisCall, String.class);
            String contentTypeKey = apiResponse.getHeaders().keySet().stream()
                .filter(header -> header.toLowerCase().contains("content-type"))
                .findFirst()
                .orElse("");

            String contentType = Optional.ofNullable(apiResponse.getHeaders().get(contentTypeKey))
                .map(list -> list.get(0))
                .orElse("");
            String textData = (String) apiResponse.getData();

            if (contentType.toLowerCase().contains("application/xml")) {
                return TransactionsParser.camtStringToLoadBookingsResponse(textData);
            } else if (contentType.toLowerCase().contains("text/plain")) {
                return TransactionsParser.mt940StringToLoadBookingsResponse(textData);
            } else {
                PaginationResolver.PaginationNextCallParameters nextCallParams = PaginationResolver.PaginationNextCallParameters.builder()
                    .bgSessionData(bgSessionData)
                    .resourceId(resourceId)
                    .consentId(consentId)
                    .bankCode(bankCode)
                    .dateFrom(dateFrom)
                    .dateTo(dateTo)
                    .withBalance(withBalance)
                    .build();
                return paginationResolver.jsonStringToLoadBookingsResponse(textData, nextCallParams);
            }
        } catch (ApiException e) {
            throw handeAisApiException(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

    private String getAccountResourceId(BgSessionData bgSessionData, String iban, String bankCode, String consentId) {
        try {
            AccountList accountList = getAccountList(bgSessionData, bankCode, consentId);

            return accountList.getAccounts()
                .stream()
                .filter(accountDetails -> accountDetails.getIban().equals(iban))
                .findAny()
                .map(AccountDetails::getResourceId)
                .orElseThrow(() -> new MultibankingException(INVALID_ACCOUNT_REFERENCE));
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
    public PaymentResponse executePayment(TransactionRequest<? extends AbstractPayment> paymentRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StrongCustomerAuthorisable getStrongCustomerAuthorisation() {
        return scaHandler;
    }

    private MultibankingException handeAisApiException(ApiException e) {
        switch (e.getCode()) {
            case 401:
                return BankingGatewayExceptionMapper.toMultibankingException(e, INVALID_PIN);
            case 404:
                return BankingGatewayExceptionMapper.toMultibankingException(e, RESOURCE_NOT_FOUND);
            case 429:
                return new MultibankingException(INVALID_CONSENT, 429, "consent access exceeded");
            default:
                return BankingGatewayExceptionMapper.toMultibankingException(e, BANKING_GATEWAY_ERROR);
        }
    }
}
