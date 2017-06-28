package figo;

import domain.*;
import me.figo.FigoConnection;
import me.figo.FigoException;
import me.figo.FigoSession;
import me.figo.internal.*;
import org.adorsys.envutils.EnvProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spi.OnlineBankingService;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static utils.Utils.getSecureRandom;

/**
 * Created by alexg on 17.05.17.
 */
public class FigoBanking implements OnlineBankingService {

    private FigoConnection figoConnection;

    private static SecureRandom random = getSecureRandom();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#%^*()-_=+[{]},<>";
    private static final Logger LOG = LoggerFactory.getLogger(FigoBanking.class);

    public enum Status {
        OK,
        SYNC,
        PIN,
        TAN,
        ERROR
    }

    public FigoBanking() {
        String figoClientId = EnvProperties.getEnvOrSysProp("FIGO_CLIENT_ID", true);
        String figoSecret = EnvProperties.getEnvOrSysProp("FIGO_SECRET", true);
        int figoTimeout = Integer.parseInt(EnvProperties.getEnvOrSysProp("FIGO_TIMEOUT", "0"));
        String figoConnectionUrl = EnvProperties.getEnvOrSysProp("FIGO_CONNECTION_URL", "https://api.figo.me");

        if (figoClientId == null || figoSecret == null) {
            LOG.warn("missing env properties FIGO_CLIENT_ID and/or FIGO_SECRET");
        } else {
            figoConnection = new FigoConnection(figoClientId, figoSecret, "http://nowhere.here", figoTimeout, figoConnectionUrl);
        }

    }

    @Override
    public BankApi bankApiIdentifier() {
        return BankApi.FIGO;
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
        bankApiUser.setBankApi(BankApi.FIGO);

        return bankApiUser;
    }

    @Override
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String pin, boolean storePin) {
        try {
            TokenResponse tokenResponse = figoConnection.credentialLogin(bankApiUser.getApiUserId() + "@admb.de", bankApiUser.getApiPassword());
            FigoSession session = new FigoSession(tokenResponse.getAccessToken());

            TaskTokenResponse response = session.setupNewAccount(
                    bankAccess.getBankCode(),
                    "de",
                    Arrays.asList(bankAccess.getBankLogin(), pin),
                    Collections.singletonList("standingOrders"),
                    storePin,
                    true
            );

            String taskToken = response.getTaskToken();
            while (checkState(session, taskToken) == Status.SYNC) {
                Thread.sleep(1000);
            }

            bankAccess.setBankName(session.getAccounts().size() > 0 ? session.getAccounts().get(0).getBankName() : null);

            return session.getAccounts().stream()
                    .filter(account -> account.getBankCode().equals(bankAccess.getBankCode()))
                    .map(account ->
                            new BankAccount()
                                    .externalId(bankApiIdentifier(), account.getAccountId())
                                    .owner(account.getOwner())
                                    .numberHbciAccount(account.getAccountNumber())
                                    .nameHbciAccount(account.getName())
                                    .bicHbciAccount(account.getBIC())
                                    .blzHbciAccount(bankAccess.getBankCode())
                                    .ibanHbciAccount(account.getIBAN())
                                    .typeHbciAccount(account.getType())
                                    .bankAccountBalance(new BankAccountBalance()
                                            .readyHbciBalance(account.getBalance().getBalance())))
                    .collect(Collectors.toList());
        } catch (IOException | FigoException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Booking> loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, BankAccount bankAccount, String pin) {
        try {
            TokenResponse tokenResponse = figoConnection.credentialLogin(bankApiUser.getApiUserId() + "@admb.de", bankApiUser.getApiPassword());
            FigoSession session = new FigoSession(tokenResponse.getAccessToken());

            TaskTokenResponse response = session.queryApi("/rest/sync",
                    new SyncTokenRequest(
                            RandomStringUtils.randomAlphanumeric(5),
                            null,
                            Collections.singletonList("standingOrders"),
                            Collections.singletonList(bankAccount.getExternalIdMap().get(bankApiIdentifier())),
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

            return session.getTransactions(bankAccount.getExternalIdMap().get(bankApiIdentifier()))
                    .stream()
                    .map(transaction -> {
                                Booking booking = new Booking();
                                booking.setExternalId(transaction.getTransactionId());
                                booking.setBankApi(bankApiIdentifier());
                                booking.setBookingDate(transaction.getBookingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                                booking.setValutaDate(transaction.getValueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                                booking.setAmount(transaction.getAmount());
                                booking.setUsage(transaction.getPurposeText());

                                if (transaction.getName() != null) {
                                    booking.setOtherAccount(new BankAccount());
                                    booking.getOtherAccount().setName(transaction.getName());
                                    booking.getOtherAccount().setCurrency(transaction.getCurrency());
                                }
                                return booking;
                            }
                    )
                    .collect(Collectors.toList());

        } catch (IOException | FigoException | InterruptedException e) {
            throw new RuntimeException(e);
        }
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
            throw new RuntimeException(taskStatus.getError().getMessage());
        }

        return Status.OK;
    }
}
