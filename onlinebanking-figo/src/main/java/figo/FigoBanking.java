package figo;

import static utils.Utils.getSecureRandom;

import me.figo.models.*;
import org.adorsys.envutils.EnvProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import domain.Bank;
import domain.BankAccess;
import domain.BankAccount;
import domain.BankAccountBalance;
import domain.BankApi;
import domain.BankApiUser;
import domain.BankLoginCredential;
import domain.Booking;
import exception.InvalidPinException;
import me.figo.FigoConnection;
import me.figo.FigoException;
import me.figo.FigoSession;
import me.figo.internal.SyncTokenRequest;
import me.figo.internal.TaskStatusRequest;
import me.figo.internal.TaskStatusResponse;
import me.figo.internal.TaskTokenResponse;
import me.figo.internal.TokenResponse;
import spi.OnlineBankingService;
import utils.Utils;

/**
 * Created by alexg on 17.05.17.
 */
public class FigoBanking implements OnlineBankingService {

    private static final String MAIL_SUFFIX = "@admb.de";
    private FigoConnection figoConnection;

    private static SecureRandom random = getSecureRandom();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#%^*()-_=+[{]},<>";
    private Logger LOG = LoggerFactory.getLogger(getClass());

    private String figoTechUser;
    private String figoTechUserCredential;

    private BankApi bankApi;

    public enum Status {
        OK,
        SYNC,
        PIN,
        TAN,
        ERROR
    }

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
            LOG = LoggerFactory.getLogger("figo.FigoBankingAlternative");
        }

        if (clientId == null || secret == null) {
            LOG.warn("missing env properties FIGO_CLIENT_ID and/or FIGO_SECRET");
        } else {
            figoConnection = new FigoConnection(clientId, secret, "http://nowhere.here", Integer.parseInt(timeout), connectionUrl);
        }

        figoTechUser = EnvProperties.getEnvOrSysProp("FIGO_TECH_USER", true);
        figoTechUserCredential = EnvProperties.getEnvOrSysProp("FIGO_TECH_USER_CREDENTIAL", true);
        if (figoTechUser == null || figoTechUserCredential == null) {
            LOG.warn("missing env properties FIGO_TECH_USER and/or FIGO_TECH_USER_CREDENTIAL");
        }
    }

    @Override
    public BankApi bankApi() {
        return bankApi;
    }

    @Override
    public boolean bankSupported(String bankCode) {
        if (figoConnection == null) {
            throw new IllegalArgumentException("figo connection not available, check env properties FIGO_CLIENT_ID and/or FIGO_SECRET");
        }
        return true;
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    @Override
    public boolean userRegistrationRequired() {
        return true;
    }

    @Override
    public BankApiUser registerUser(String uid) {
        if (figoConnection == null) {
            throw new IllegalArgumentException("figo connection not available, check env properties FIGO_CLIENT_ID and/or FIGO_SECRET");
        }

        String password = RandomStringUtils.random(20, 0, 0, false, false, CHARACTERS.toCharArray(), random);

        try {
            figoConnection.addUser(uid, uid + "@admb.de", password, "de");
        } catch (IOException | FigoException e) {
            throw new RuntimeException(e);
        }

        BankApiUser bankApiUser = new BankApiUser();
        bankApiUser.setApiUserId(uid);
        bankApiUser.setApiPassword(password);
        bankApiUser.setBankApi(bankApi());

        return bankApiUser;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
        try {
            TokenResponse tokenResponse = figoConnection.credentialLogin(bankApiUser.getApiUserId() + MAIL_SUFFIX, bankApiUser.getApiPassword());
            FigoSession session = createSession(tokenResponse.getAccessToken());
            session.removeUser();
        } catch (IOException | FigoException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Bank getBankLoginSettings(String bankCode) {
        FigoSession figoSession = loginTechUser();

        BankLoginSettings figoBankLoginSettings;
        try {
            figoBankLoginSettings = figoSession.queryApi("/rest/catalog/banks/de/" + bankCode, null, "GET", BankLoginSettings.class);
        } catch (IOException | FigoException e) {
            throw new RuntimeException(e);
        }
        Bank bank = new Bank();
        bank.setName(figoBankLoginSettings.getBankName());

        domain.BankLoginSettings loginSettings = new domain.BankLoginSettings();
        bank.setLoginSettings(loginSettings);

        loginSettings.setAdditional_icons(figoBankLoginSettings.getAdditionalIcons());
        loginSettings.setAdvice(figoBankLoginSettings.getAdvice());
        loginSettings.setAuth_type(figoBankLoginSettings.getAuthType());
        loginSettings.setIcon(figoBankLoginSettings.getIcon());
//        loginSettings.setSupported(figoBankLoginSettings.isSupported());
        loginSettings.setCredentials(new ArrayList<>());

        figoBankLoginSettings.getCredentials().forEach(credential -> {
            BankLoginCredential bankLoginCredential = new BankLoginCredential();
            bankLoginCredential.setLabel(credential.getLabel());
            bankLoginCredential.setMasked(credential.isMasked());
            bankLoginCredential.setOptional(credential.isOptional());

            loginSettings.getCredentials().add(bankLoginCredential);
        });

        return bank;
    }

    @Override
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, boolean storePin) {
        try {
            TokenResponse tokenResponse = figoConnection.credentialLogin(bankApiUser.getApiUserId() + MAIL_SUFFIX, bankApiUser.getApiPassword());
            FigoSession session = createSession(tokenResponse.getAccessToken());

            TaskTokenResponse response = session.setupNewAccount(
                    bankAccess.getBankCode(),
                    "de",
                    createCredentials(
                            bankAccess.getBankLogin(),
                            bankAccess.getBankLogin2(),
                            pin
                    ),
                    Collections.singletonList("standingOrders"),
                    storePin,
                    true
            );

            String taskToken = response.getTaskToken();
            while (checkState(session, taskToken) == Status.SYNC) {
                Thread.sleep(1000);
            }

            return session.getAccounts().stream()
                    .map(this::mapBankAccount)
                    .collect(Collectors.toList());
        } catch (IOException | FigoException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private BankAccountBalance getBalance(FigoSession figoSession, String accountId) {
        try {
            Account account = figoSession.getAccount(accountId);
            AccountBalance accountBalance = account.getBalance();
            return new BankAccountBalance().readyHbciBalance(accountBalance.getBalance());
        } catch (IOException | FigoException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        try {
            TokenResponse tokenResponse = figoConnection.credentialLogin(bankApiUser.getApiUserId() + MAIL_SUFFIX,
                bankApiUser.getApiPassword());
            FigoSession session = createSession(tokenResponse.getAccessToken());

            session.removeAccount(bankAccount.getExternalIdMap().get(bankApi()));
        } catch (IOException | FigoException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Booking> loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin) {
        try {
            TokenResponse tokenResponse = figoConnection.credentialLogin(bankApiUser.getApiUserId() + "@admb.de", bankApiUser.getApiPassword());
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

            Status status = waitForFinish(session, response.getTaskToken(), pin);
            if (status == Status.PIN) {
                TaskStatusRequest request = new TaskStatusRequest(response.getTaskToken());
                request.setPin(pin);
                session.queryApi("/task/progress?id=" + response.getTaskToken(), request, "POST", TaskStatusResponse.class);
                waitForFinish(session, response.getTaskToken(), pin);
            }

            List<Booking> bookings = session.getTransactions(bankAccount.getExternalIdMap().get(bankApi()))
                    .stream()
                    .map(this::mapBooking)
                    .collect(Collectors.toList());

            List<StandingOrder> standingOrders = session.getStandingOrders(bankAccount.getExternalIdMap().get(bankApi()));

            bankAccount.setBankAccountBalance(getBalance(session, bankAccount.getExternalIdMap().get(bankApi())));

            bookings.forEach(booking ->
                    standingOrders
                            .stream()
                            .filter(so -> so.getAmount().negate().compareTo(booking.getAmount()) == 0 &&
                                    Utils.inCycle(booking.getValutaDate(), so.getExecutionDay()) &&
                                    Utils.usageContains(booking.getUsage(), so.getPurposeText())
                            )
                            .findFirst()
                            .ifPresent(standingOrder -> {
                                booking.setStandingOrder(true);
                            }));

            return bookings;

        } catch (IOException | FigoException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private BankAccount mapBankAccount(Account account) {
        return new BankAccount()
            .externalId(bankApi(), account.getAccountId())
            .owner(account.getOwner())
            .numberHbciAccount(account.getAccountNumber())
            .nameHbciAccount(account.getName())
            .bankName(account.getBankName())
            .bicHbciAccount(account.getBIC())
            .blzHbciAccount(account.getBankCode())
            .ibanHbciAccount(account.getIBAN())
            .typeHbciAccount(account.getType())
            .bankAccountBalance(new BankAccountBalance()
                .readyHbciBalance(account.getBalance().getBalance()));
    }

    private Booking mapBooking(Transaction transaction) {
        Booking booking = new Booking();
        booking.setExternalId(transaction.getTransactionId());
        booking.setBankApi(bankApi());
        booking.setBookingDate(transaction.getBookingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        booking.setValutaDate(transaction.getValueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        booking.setAmount(transaction.getAmount());
        booking.setUsage(transaction.getPurposeText());
        booking.setText(transaction.getBookingText());
        booking.setTransactionCode(transaction.getTransactionCode());
        booking.setOtherAccount(mapBookingAccount(transaction));
        booking.setCreditorId(Utils.extractCreditorId(transaction.getPurposeText()));
        booking.setMandateReference(Utils.extractMandateReference(transaction.getPurposeText()));
        return booking;
    }

    private BankAccount mapBookingAccount(Transaction transaction) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setName(transaction.getName());
        bankAccount.setBankName(transaction.getBankName());
        bankAccount.setCurrency(transaction.getCurrency());
        bankAccount.setAccountNumber(transaction.getAccountNumber());
        bankAccount.setBlz(transaction.getBankCode());
        return bankAccount;
    }

    private Status waitForFinish(FigoSession session, String taskToken, String pin) throws IOException, FigoException, InterruptedException {
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
            LOG.info("figo.getTaskState {} {}", taskStatus.getAccountId(), taskStatus.getMessage());
        } catch (IOException | FigoException e) {
            throw new RuntimeException(e);
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
                throw new InvalidPinException();
            }
            throw new RuntimeException(taskStatus.getError().getMessage());
        }

        return Status.OK;
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
                throw new RuntimeException(e);
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
}
