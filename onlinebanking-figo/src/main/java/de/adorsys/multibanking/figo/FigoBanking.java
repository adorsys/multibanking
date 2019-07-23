package de.adorsys.multibanking.figo;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.*;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import me.figo.FigoConnection;
import me.figo.FigoException;
import me.figo.FigoSession;
import me.figo.internal.*;
import me.figo.models.Account;
import me.figo.models.AccountBalance;
import me.figo.models.BankLoginSettings;
import org.adorsys.envutils.EnvProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_PIN;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_TAN;
import static de.adorsys.multibanking.domain.utils.Utils.getSecureRandom;

/**
 * Created by alexg on 17.05.17.
 */
public class FigoBanking implements OnlineBankingService {

    private static final String MAIL_SUFFIX = "@admb.de";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#%^*()" +
            "-_=+[{]},<>";
    private static SecureRandom random = getSecureRandom();
    private FigoConnection figoConnection;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String figoTechUser;
    private String figoTechUserCredential;

    private BankApi bankApi;

    public FigoBanking(BankApi bankApi) {
        this.bankApi = bankApi;

        String clientId = EnvProperties.getEnvOrSysProp("FIGO_CLIENT_ID", true);
        String secret = EnvProperties.getEnvOrSysProp("FIGO_SECRET", true);
        String timeout = EnvProperties.getEnvOrSysProp("FIGO_TIMEOUT", "0");
        String connectionUrl = EnvProperties.getEnvOrSysProp("FIGO_CONNECTION_URL", "https://api.figo.me");

        if (bankApi == BankApi.FIGO_ALTERNATIVE) {
            clientId = EnvProperties.getEnvOrSysProp("FIGO_ALTERNATIVE_CLIENT_ID", clientId);
            secret = EnvProperties.getEnvOrSysProp("FIGO_ALTERNATIVE_SECRETT", secret);
            timeout = EnvProperties.getEnvOrSysProp("FIGO_ALTERNATIVE_TIMEOUT", timeout);
            connectionUrl = EnvProperties.getEnvOrSysProp("FIGO_ALTERNATIVE_CONNECTION_URL", connectionUrl);
            logger = LoggerFactory.getLogger("figo.FigoBankingAlternative");
        }

        if (clientId == null || secret == null) {
            logger.warn("missing env properties FIGO_CLIENT_ID and/or FIGO_SECRET");
        } else {
            figoConnection = new FigoConnection(clientId, secret, "http://nowhere.here", Integer.parseInt(timeout),
                    connectionUrl);
        }

        figoTechUser = EnvProperties.getEnvOrSysProp("FIGO_TECH_USER", true);
        figoTechUserCredential = EnvProperties.getEnvOrSysProp("FIGO_TECH_USER_CREDENTIAL", true);
        if (figoTechUser == null || figoTechUserCredential == null) {
            logger.warn("missing env properties FIGO_TECH_USER and/or FIGO_TECH_USER_CREDENTIAL");
        }
    }

    @Override
    public BankApi bankApi() {
        return bankApi;
    }

    @Override
    public boolean externalBankAccountRequired() {
        return true;
    }

    @Override
    public boolean bankSupported(String bankCode) {
        if (figoConnection == null) {
            throw new IllegalArgumentException("figo connection not available, check env properties FIGO_CLIENT_ID " +
                    "and/or FIGO_SECRET");
        }
        return true;
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    @Override
    public InitiatePaymentResponse initiatePayment(String bankingUrl, TransactionRequest paymentRequest) {
        return null;
    }

    @Override
    public void executeTransactionWithoutSca(String bankingUrl, TransactionRequest paymentRequest) {
    }

    @Override
    public AuthorisationCodeResponse requestAuthorizationCode(String bankingUrl, TransactionRequest paymentRequest) {
        return null;
    }

    @Override
    public SubmitAuthorizationCodeResponse submitAuthorizationCode(SubmitAuthorizationCodeRequest submitPaymentRequest) {
        return null;
    }

    @Override
    public boolean userRegistrationRequired() {
        return true;
    }

    @Override
    public BankApiUser registerUser(String bankingUrl, BankAccess bankAccess, String pin) {
        if (figoConnection == null) {
            throw new IllegalArgumentException("figo connection not available, check env properties FIGO_CLIENT_ID " +
                    "and/or FIGO_SECRET");
        }

        String password = RandomStringUtils.random(20, 0, 0, false, false, CHARACTERS.toCharArray(), random);
        String email = bankAccess.getBankLogin() + RandomStringUtils.randomAlphanumeric(10) + MAIL_SUFFIX;

        try {
            figoConnection.addUser(bankAccess.getBankLogin(), email, password, "de");
        } catch (IOException | FigoException e) {
            throw new IllegalStateException(e);
        }

        BankApiUser bankApiUser = new BankApiUser();
        bankApiUser.setApiUserId(bankAccess.getBankLogin());
        bankApiUser.setApiPassword(password);
        bankApiUser.setBankApi(bankApi());

        return bankApiUser;
    }

    @Override
    public void removeUser(String bankingUrl, BankApiUser bankApiUser) {
        try {
            TokenResponse tokenResponse = figoConnection.credentialLogin(bankApiUser.getApiUserId() + MAIL_SUFFIX,
                    bankApiUser.getApiPassword());
            FigoSession session = createSession(tokenResponse.getAccessToken());
            session.removeUser();
        } catch (IOException | FigoException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public ScaMethodsResponse authenticatePsu(String bankingUrl, AuthenticatePsuRequest authenticatePsuRequest) {
        return null;
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(String bankingUrl,
                                                           LoadAccountInformationRequest loadAccountInformationRequest) {

        BankApiUser bankApiUser = loadAccountInformationRequest.getBankApiUser();
        BankAccess bankAccess = loadAccountInformationRequest.getBankAccess();

        try {
            TokenResponse tokenResponse = figoConnection.credentialLogin(bankApiUser.getApiUserId() + MAIL_SUFFIX,
                    bankApiUser.getApiPassword());
            FigoSession session = createSession(tokenResponse.getAccessToken());

            TaskTokenResponse response = session.setupNewAccount(
                    bankAccess.getBankCode(),
                    "de",
                    createCredentials(
                            bankAccess.getBankLogin(),
                            bankAccess.getBankLogin2(),
                            loadAccountInformationRequest.getPin()
                    ),
                    Collections.singletonList("standingOrders"),
                    loadAccountInformationRequest.isStorePin(),
                    true
            );

            String taskToken = response.getTaskToken();
            while (checkState(session, taskToken) == Status.SYNC) {
                Thread.sleep(1000);
            }

            updateTanTransportTypes(bankAccess, session.getAccounts());

            return LoadAccountInformationResponse.builder()
                    .bankAccounts(session.getAccounts().stream()
                            .map(account -> FigoMapping.mapBankAccount(account, bankApi))
                            .collect(Collectors.toList()))
                    .build();
        } catch (IOException | FigoException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public Bank getBankLoginSettings(String bankCode) {
        FigoSession figoSession = loginTechUser();

        BankLoginSettings figoBankLoginSettings;
        try {
            figoBankLoginSettings = figoSession.queryApi("/rest/catalog/banks/de/" + bankCode, null, "GET",
                    BankLoginSettings.class);
        } catch (IOException | FigoException e) {
            throw new IllegalStateException(e);
        }
        Bank bank = new Bank();
        bank.setName(figoBankLoginSettings.getBankName());

        de.adorsys.multibanking.domain.BankLoginSettings loginSettings =
                new de.adorsys.multibanking.domain.BankLoginSettings();
        bank.setLoginSettings(loginSettings);

        loginSettings.setAdvice(figoBankLoginSettings.getAdvice());
        loginSettings.setAuth_type(figoBankLoginSettings.getAuthType());
        loginSettings.setIcon(figoBankLoginSettings.getIcon());
//        loginSettings.setSupported(figoBankLoginSettings.isSupported());
        loginSettings.setCredentials(new ArrayList<>());

        figoBankLoginSettings.getCredentials().forEach(credential -> {
            BankLoginCredentialInfo bankLoginCredential = new BankLoginCredentialInfo();
            bankLoginCredential.setLabel(credential.getLabel());
            bankLoginCredential.setMasked(credential.isMasked());
            bankLoginCredential.setOptional(credential.isOptional());

            loginSettings.getCredentials().add(bankLoginCredential);
        });

        return bank;
    }

    private BalancesReport getBalance(FigoSession figoSession, String accountId) {
        try {
            Account account = figoSession.getAccount(accountId);
            AccountBalance accountBalance = account.getBalance();
            return new BalancesReport().readyBalance(Balance.builder().amount(accountBalance.getBalance()).build());
        } catch (IOException | FigoException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void removeBankAccount(String bankingUrl, BankAccount bankAccount, BankApiUser bankApiUser) {
        try {
            TokenResponse tokenResponse = figoConnection.credentialLogin(bankApiUser.getApiUserId() + MAIL_SUFFIX,
                    bankApiUser.getApiPassword());
            FigoSession session = createSession(tokenResponse.getAccessToken());

            session.removeAccount(bankAccount.getExternalIdMap().get(bankApi()));
        } catch (IOException | FigoException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public LoadBookingsResponse loadBookings(String bankingUrl, LoadBookingsRequest loadBookingsRequest) {
        BankApiUser bankApiUser = loadBookingsRequest.getBankApiUser();
        BankAccount bankAccount = loadBookingsRequest.getBankAccount();

        try {
            TokenResponse tokenResponse = figoConnection.credentialLogin(bankApiUser.getApiUserId() + "@admb.de",
                    bankApiUser.getApiPassword());
            FigoSession session = createSession(tokenResponse.getAccessToken());

            TaskTokenResponse response = session.queryApi("/rest/sync",
                    new SyncTokenRequest(
                            RandomStringUtils.randomAlphanumeric(5),
                            null,
                            Collections.singletonList("standingOrders"),
                            Collections.singletonList(bankAccount.getExternalIdMap().get(bankApi())),
                            true,  // disable_notifications
                            0,     // if_not_synced_since
                            false  // auto_continue
                    ),
                    "POST", TaskTokenResponse.class);

            Status status = waitForFinish(session, response.getTaskToken());
            if (status == Status.PIN) {
                submitPin(response.getTaskToken(), loadBookingsRequest.getPin(), session);
            }

            List<Booking> bookings = session.getTransactions(bankAccount.getExternalIdMap().get(bankApi()))
                    .stream()
                    .map(transaction -> FigoMapping.mapBooking(transaction, bankApi))
                    .collect(Collectors.toList());

            List<StandingOrder> standingOrders =
                    session.getStandingOrders(bankAccount.getExternalIdMap().get(bankApi()))
                            .stream()
                            .map(FigoMapping::mapStandingOrder)
                            .collect(Collectors.toList());

            updateTanTransportTypes(loadBookingsRequest.getBankAccess(), session.getAccounts());

            return LoadBookingsResponse.builder()
                    .bookings(bookings)
                    .standingOrders(standingOrders)
                    .bankAccountBalance(getBalance(session, bankAccount.getExternalIdMap().get(bankApi())))
                    .build();

        } catch (IOException | FigoException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<BankAccount> loadBalances(String bankingUrl, LoadBalanceRequest loadBalanceRequest) {
        return null;
    }

    @Override
    public boolean accountInformationConsentRequired() {
        return false;
    }

    @Override
    public CreateConsentResponse createAccountInformationConsent(String bankingUrl,
                                                                 CreateConsentRequest createConsentRequest) {

        return null;
    }

    private TaskStatusResponse submitPin(String taskToken, String pin, FigoSession session) throws FigoException,
            InterruptedException, IOException {
        TaskStatusResponse response = session.queryApi("/task/progress?id=" + taskToken,
                new TaskStatusRequest(taskToken, pin), "POST", TaskStatusResponse.class);
        Status status = waitForFinish(session, taskToken);

        if (status != Status.OK && status != Status.TAN) {
            throw new MultibankingException(INVALID_PIN, response.getMessage());
        }
        return response;
    }

    private void submitTan(String taskToken, String tan, FigoSession session) throws FigoException,
            InterruptedException, IOException {
        TaskStatusRequest taskStatusRequest = new TaskStatusRequest(taskToken);
        taskStatusRequest.setResponse(tan);

        session.queryApi("/task/progress?id=" + taskToken, taskStatusRequest, "POST", TaskStatusResponse.class);
        Status status = waitForFinish(session, taskToken);

        if (status != Status.OK) {
            throw new MultibankingException(INVALID_TAN, Collections.emptyList());
        }
    }

    private void updateTanTransportTypes(BankAccess bankAccess, List<Account> accounts) {
        List<TanTransportType> tanTransportTypes = accounts
                .stream()
                .map(Account::getSupportedTanSchemes)
                .flatMap(Collection::stream)
                .map(FigoMapping::mapTanTransportTypes)
                .collect(Collectors.toList());
        if (bankAccess.getTanTransportTypes() == null) {
            bankAccess.setTanTransportTypes(new HashMap<>());
        }

        bankAccess.getTanTransportTypes().put(bankApi(), tanTransportTypes);
    }

    private Status waitForFinish(FigoSession session, String taskToken) throws IOException, FigoException,
            InterruptedException {
        Status status;
        while ((status = checkState(session, taskToken)) == Status.SYNC) {
            Thread.sleep(1000);
        }

        return status;
    }

    private Status checkState(FigoSession figoSession, String taskToken) throws IOException, FigoException {
        TaskStatusResponse taskStatus;
        try {
            taskStatus = figoSession.getTaskState(taskToken);
            logger.info("figo.getTaskState {} {}", taskStatus.getAccountId(), taskStatus.getMessage());
        } catch (IOException | FigoException e) {
            throw new IllegalStateException(e);
        }

        return resolveStatus(taskStatus);
    }

    private Status resolveStatus(TaskStatusResponse taskStatus) throws IOException, FigoException {
        if (!taskStatus.isEnded() && !taskStatus.isErroneous() && !taskStatus.isWaitingForPin()
                && !taskStatus.isWaitingForResponse()) {
            return Status.SYNC;
        }

        if (taskStatus.isWaitingForPin()) {
            return Status.PIN;
        }

        if (taskStatus.isWaitingForResponse()) {
            return Status.TAN;
        }

        if (taskStatus.isErroneous()) {
            if (taskStatus.getError().getCode() == 10000 || taskStatus.getError().getCode() == 10001) {
                throw new MultibankingException(INVALID_PIN, taskStatus.getError().getMessage());
            }
            throw new IllegalStateException(taskStatus.getError().getMessage());
        }

        return Status.OK;
    }

    String extractTaskToken(URL url) throws UnsupportedEncodingException {
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            if (pair.startsWith("id=")) {
                String id = pair.substring(3);
                return URLDecoder.decode(id, "UTF-8");
            }
        }
        return null;
    }

    /**
     * Erzeugt eine ZB-Session mit dem technischen Figo-User.
     */
    private FigoSession loginTechUser() {
        String username = figoTechUser + MAIL_SUFFIX;
        String accessToken;

        try {
            accessToken = figoConnection.credentialLogin(username, figoTechUserCredential).getAccessToken();
        } catch (Exception e) {
            //login not possible, try create technical user
            try {
                figoConnection.addUser(figoTechUser, username, figoTechUserCredential, "de");
                accessToken = figoConnection.credentialLogin(username, figoTechUserCredential).getAccessToken();
            } catch (Exception e1) {
                throw new IllegalStateException(e);
            }
        }

        return createSession(accessToken);
    }

    private FigoSession createSession(String accessToken) {
        return new FigoSession(accessToken, figoConnection.getTimeout(), figoConnection.getApiEndpoint());
    }

    private List<String> createCredentials(String... credentials) {
        return Arrays.stream(credentials)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public enum Status {
        OK,
        SYNC,
        PIN,
        TAN,
        ERROR
    }
}
