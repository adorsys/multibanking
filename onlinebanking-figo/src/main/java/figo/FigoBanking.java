package figo;

import domain.*;
import me.figo.FigoConnection;
import me.figo.FigoException;
import me.figo.FigoSession;
import me.figo.internal.SyncTokenRequest;
import me.figo.internal.TaskStatusResponse;
import me.figo.internal.TaskTokenResponse;
import me.figo.internal.TokenResponse;
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
        String figoClientId = EnvProperties.getEnvOrSysProp("figoClientId", "CKmGgL2cUq8fL-IaTM3jloNzIqptWogQYCGolQT-9r7Y");
        String figoSecret = EnvProperties.getEnvOrSysProp("figoSecret", "S-Y7598_mYfjxo0vYLVpk52YYfom-Fxo0_OQ8HSCdfmY");
        int figoTimeout = Integer.parseInt(EnvProperties.getEnvOrSysProp("figoTimeout", "0"));
        String figoConnectionUrl = EnvProperties.getEnvOrSysProp("figoConnectionUrl", "https://api.figo.me");

        figoConnection = new FigoConnection(figoClientId, figoSecret, "http://nowhere.here", figoTimeout, figoConnectionUrl);
    }

    @Override
    public BankApi bankApiIdentifier() {
        return BankApi.FIGO;
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
    public boolean userRegistrationRequired() {
        return true;
    }

    @Override
    public BankApiUser registerUser(String uid) {
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
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String pin) {
        try {
            TokenResponse tokenResponse = figoConnection.credentialLogin(bankApiUser.getApiUserId() + "@admb.de", bankApiUser.getApiPassword());
            FigoSession session = new FigoSession(tokenResponse.getAccessToken());

            TaskTokenResponse response = session.setupNewAccount(
                    bankAccess.getBankCode(),
                    "de",
                    Arrays.asList(bankAccess.getBankLogin(), pin),
                    Collections.singletonList("standingOrders"),
                    true,
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

            String taskToken = response.getTaskToken();
            while (checkState(session, taskToken) == Status.SYNC) {
                Thread.sleep(1000);
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
                                    booking.getOtherAccount().setNameHbciAccount(transaction.getName());
                                    booking.getOtherAccount().setCurrencyHbciAccount(transaction.getCurrency());
                                }
                                return booking;
                            }
                    )
                    .collect(Collectors.toList());

        } catch (IOException | FigoException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Status checkState(FigoSession figoSession, String taskToken) {
        TaskStatusResponse taskStatus;
        try {
            taskStatus = figoSession.getTaskState(taskToken);
            LOG.info("figo.getTaskState {} {}", taskStatus.getAccountId(), taskStatus.getMessage());
        } catch (IOException | FigoException e) {
            throw new RuntimeException(e);
        }

        return resolveStatus(taskStatus);
    }

    private Status resolveStatus(TaskStatusResponse taskStatus) {
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
