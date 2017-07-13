package finapi;

import domain.*;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.*;
import io.swagger.client.model.*;
import org.adorsys.envutils.EnvProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spi.OnlineBankingService;

import java.security.SecureRandom;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static utils.Utils.getSecureRandom;

/**
 * Created by alexg on 17.05.17.
 */
public class FinapiBanking implements OnlineBankingService {

    private String finapiClientId;
    private String finapiSecret;
    private String finapiConnectionUrl;
    private InlineResponse20024 clientToken;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static SecureRandom random = getSecureRandom();
    //https://finapi.zendesk.com/hc/en-us/articles/222013148
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789&(){}[].:,?!+-_$@#";

    private static final Logger LOG = LoggerFactory.getLogger(FinapiBanking.class);

    public FinapiBanking() {
        finapiClientId = EnvProperties.getEnvOrSysProp("FINAPI_CLIENT_ID", true);
        finapiSecret = EnvProperties.getEnvOrSysProp("FINAPI_SECRET", true);
        finapiConnectionUrl = EnvProperties.getEnvOrSysProp("FINAPI_CONNECTION_URL", "https://sandbox.finapi.io/");

        if (finapiClientId == null || finapiSecret == null) {
            LOG.warn("missing env properties FINAPI_CLIENT_ID and/or FINAPI_SECRET");
        } else {
            authorizeClient();
        }
    }

    @Override
    public BankApi bankApi() {
        return BankApi.FINAPI;
    }

    @Override
    public boolean userRegistrationRequired() {
        return true;
    }

    @Override
    public boolean bankSupported(String bankCode) {
        if (clientToken == null) {
            LOG.warn("skip finapi bank api, client token not available, check env properties FINAPI_CLIENT_ID and/or FINAPI_SECRET");
        }
        return true;
    }

    @Override
    public BankApiUser registerUser(String uid) {
        String password = RandomStringUtils.random(20, 0, 0, false, false, CHARACTERS.toCharArray(), random);

        try {
            new UsersApi(createApiClient()).createUser(new Body18().email(uid + "@admb.de").password(password).id(uid));
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        BankApiUser bankApiUser = new BankApiUser();
        bankApiUser.setApiUserId(uid);
        bankApiUser.setApiPassword(password);
        bankApiUser.setBankApi(BankApi.FINAPI);

        return bankApiUser;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
        try {
            new UsersApi(createApiClient()).deleteUnverifiedUser(bankApiUser.getApiUserId());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BankLoginSettings getBankLoginSettings(String bankCode) {
        //TODO
        return null;
    }

    @Override
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String pin, boolean storePin) {
        LOG.info("load bank accounts");
        try {
            InlineResponse2006 searchAllBanks = new BanksApi(createApiClient()).getAndSearchAllBanks(null, bankAccess.getBankCode(), null, null, null, null, null);
            if (searchAllBanks.getBanks().size() != 1) {
                throw new RuntimeException("Bank not supported");
            }

            bankAccess.setBankName(searchAllBanks.getBanks().get(0).getName());

            ApiClient apiClient = createUserApiClient();
            apiClient.setAccessToken(authorizeUser(bankApiUser));

            InlineResponse2005Connections connections = new BankConnectionsApi(apiClient).importBankConnection(new Body3()
                    .bankId(searchAllBanks.getBanks().get(0).getId())
                    .bankingUserId(bankAccess.getBankLogin())
                    .bankingPin(pin)
                    .storePin(storePin));

            bankAccess.externalId(bankApi(), connections.getId().toString());

            InlineResponse200 accounts = new AccountsApi(apiClient).getAndSearchAllAccounts(null, null, null, Arrays.asList(connections.getId()), null, null, null, null);
            return accounts.getAccounts().stream().map(account ->
                    new BankAccount()
                            .externalId(bankApi(), account.getId().toString())
                            .owner(account.getAccountHolderName())
                            .bankName(bankAccess.getBankName())
                            .numberHbciAccount(account.getAccountNumber())
                            .nameHbciAccount(account.getAccountName())
                            .ibanHbciAccount(account.getIban())
                            .blzHbciAccount(bankAccess.getBankCode())
                            .typeHbciAccount(account.getAccountTypeName())
                            .bankAccountBalance(new BankAccountBalance()
                                    .readyHbciBalance(account.getBalance())))
                    .collect(Collectors.toList());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        ApiClient apiClient = createUserApiClient();
        apiClient.setAccessToken(authorizeUser(bankApiUser));

        try {
            new AccountsApi(apiClient).deleteAccount(Long.parseLong(bankAccount.getExternalIdMap().get(bankApi())));
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Booking> loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, BankAccount bankAccount, String pin) {
        LOG.debug("load bookings for account [{}]", bankAccount.getAccountNumber());
        ApiClient apiClient = createUserApiClient();
        apiClient.setAccessToken(authorizeUser(bankApiUser));

        List<Long> accountIds = Arrays.asList(Long.parseLong(bankAccount.getExternalIdMap().get(bankApi())));
        List<String> order = Arrays.asList("id,desc");

        List<Booking> bookingList = new ArrayList<>();
        InlineResponse20019 transactionsResponse = null;
        Integer nextPage = null;
        try {
            //wait finapi loaded bookings
            InlineResponse200Accounts account = waitAccountSynced(bankAccount, apiClient);
            //wait finapi categorized bookings
            waitBookingsCategorized(bankAccess, apiClient);

            bankAccount.bankAccountBalance(new BankAccountBalance().readyHbciBalance(account.getBalance()));

            while (nextPage == null || transactionsResponse.getPaging().getPage() < transactionsResponse.getPaging().getPageCount()) {
                transactionsResponse = new TransactionsApi(apiClient).getAndSearchAllTransactions("bankView", null, null, null, accountIds, null, null, null, null, null, null, null, null, null, null, null, null, null, null, nextPage, null, order);
                nextPage = transactionsResponse.getPaging().getPage() + 1;
                bookingList.addAll(transactionsResponse.getTransactions().stream().map(transaction -> {
                            Booking booking = new Booking();
                            booking.setExternalId(transaction.getId().toString());
                            booking.setBankApi(bankApi());
                            booking.setBookingDate(LocalDate.from(formatter.parse(transaction.getBankBookingDate(), new ParsePosition(0))));
                            booking.setValutaDate(LocalDate.from(formatter.parse(transaction.getValueDate(), new ParsePosition(0))));

                            booking.setAmount(transaction.getAmount());
                            booking.setUsage(transaction.getPurpose());

                            if (transaction.getCounterpartName() != null) {
                                booking.setOtherAccount(new BankAccount());
                                booking.getOtherAccount().setName(transaction.getCounterpartName());
                                booking.getOtherAccount().setAccountNumber(transaction.getCounterpartAccountNumber());
                            }

                            if (transaction.getCategory() != null) {
                                BookingCategory bookingCategory = new BookingCategory();
                                bookingCategory.setMainCategory(transaction.getCategory().getParentName());
                                bookingCategory.setSubCategory(transaction.getCategory().getName());
                                booking.setBookingCategory(bookingCategory);
                            }

                            return booking;
                        }
                ).collect(Collectors.toList()));
            }
            LOG.info("loaded [{}] bookings for account [{}]", bookingList.size(), bankAccount.getAccountNumber());
            return bookingList;
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    private InlineResponse200Accounts waitAccountSynced(BankAccount bankAccount, ApiClient apiClient) throws ApiException {
        InlineResponse200Accounts account = new AccountsApi(apiClient).getAccount(Long.parseLong(bankAccount.getExternalIdMap().get(bankApi())));
        while (account.getStatus() == InlineResponse200Accounts.StatusEnum.DOWNLOAD_IN_PROGRESS) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            account = new AccountsApi(apiClient).getAccount(Long.parseLong(bankAccount.getExternalIdMap().get(bankApi())));
        }
        return account;
    }

    private void waitBookingsCategorized(BankAccess bankAccess, ApiClient apiClient) throws ApiException {
        InlineResponse2005Connections connection = new BankConnectionsApi(apiClient).getBankConnection(Long.parseLong(bankAccess.getExternalIdMap().get(bankApi())));
        while (connection.getCategorizationStatus() != InlineResponse2005Connections.CategorizationStatusEnum.READY) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connection = new BankConnectionsApi(apiClient).getBankConnection(Long.parseLong(bankAccess.getExternalIdMap().get(bankApi())));
        }
    }

    @Override
    public boolean bookingsCategorized() {
        return true;
    }

    private ApiClient createApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(finapiConnectionUrl);
        apiClient.setAccessToken(clientToken != null ? clientToken.getAccessToken() : null);
        return apiClient;
    }

    private ApiClient createUserApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(finapiConnectionUrl);
        return apiClient;
    }

    private String authorizeUser(BankApiUser bankApiUser) {
        try {
            return new AuthorizationApi(createApiClient()).getToken("password", finapiClientId, finapiSecret, null, bankApiUser.getApiUserId(), bankApiUser.getApiPassword()).getAccessToken();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void authorizeClient() {
        try {
            clientToken = new AuthorizationApi(createApiClient()).getToken("client_credentials", finapiClientId, finapiSecret, null, null, null);
        } catch (ApiException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
