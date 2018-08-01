package hbci4java;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import domain.*;
import exception.InvalidPinException;
import exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.GV_Result.GVRTANMediaList;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spi.OnlineBankingService;
import utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.kapott.hbci.manager.HBCIJobFactory.newJob;

@Slf4j
public class Hbci4JavaBanking implements OnlineBankingService {

    private static final Logger LOG = LoggerFactory.getLogger(Hbci4JavaBanking.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Hbci4JavaBanking() {
        try (InputStream inputStream = HBCIUtils.class.getClassLoader().getResource("blz.properties").openStream()) {
            HBCIUtils.refreshBLZList(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new Jdk8Module());
    }

    @Override
    public BankApi bankApi() {
        return BankApi.HBCI;
    }

    @Override
    public boolean externalBankAccountRequired() {
        return false;
    }

    @Override
    public boolean userRegistrationRequired() {
        return false;
    }

    @Override
    public BankApiUser registerUser(String uid) {
        //no registration needed
        return null;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
        //not needed
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    @Override
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, boolean storePin) {
        LOG.info("Loading Account list for access {}", bankAccess.getBankCode());
        try {
            HBCIDialog dialog = createDialog(bankAccess, bankCode, null, pin);

            if (dialog.getPassport().jobSupported("SEPAInfo")) {
                log.info("fetching SEPA information");
                dialog.addTask(newJob("SEPAInfo", dialog.getPassport()));
            }

            // TAN-Medien abrufen
            if (dialog.getPassport().jobSupported("TANMediaList")) {
                log.info("fetching TAN media list");
                dialog.addTask(newJob("TANMediaList", dialog.getPassport()));
            }
            dialog.execute(true);

            bankAccess.setBankName(dialog.getPassport().getInstName());
            List<BankAccount> hbciAccounts = new ArrayList<>();
            for (Konto konto : dialog.getPassport().getAccounts()) {
                BankAccount bankAccount = HbciMapping.toBankAccount(konto);
                bankAccount.externalId(bankApi(), UUID.randomUUID().toString());
                bankAccount.bankName(bankAccess.getBankName());
                hbciAccounts.add(bankAccount);
            }
            bankAccess.setHbciPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
            return hbciAccounts;
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //not needed
    }

    @Override
    public LoadBookingsResponse loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin) {
        HBCIDialog dialog = createDialog(bankAccess, bankCode, null, pin);
        try {
            Konto account = dialog.getPassport().getAccount(bankAccount.getAccountNumber());
            account.iban = bankAccount.getIban();
            account.bic = bankAccount.getBic();

            AbstractHBCIJob balanceJob = newJob("SaldoReq", dialog.getPassport());
            balanceJob.setParam("my", account);
            dialog.addTask(balanceJob);

            AbstractHBCIJob bookingsJob = newJob("KUmsAll", dialog.getPassport());
            bookingsJob.setParam("my", account);
            if (bankAccount.getLastSync() != null) {
                bookingsJob.setParam("startdate", Date.from(bankAccount.getLastSync().atZone(ZoneId.systemDefault()).toInstant()));
            }
            dialog.addTask(bookingsJob);

            AbstractHBCIJob standingOrdersJob = null;
            if (dialog.getPassport().jobSupported("DauerSEPAList")) {
                standingOrdersJob = newJob("DauerSEPAList", dialog.getPassport());
                standingOrdersJob.setParam("src", account);
                dialog.addTask(standingOrdersJob);
            }

            // Let the Handler execute all jobs in one batch
            HBCIExecStatus status = dialog.execute(true);
            if (!status.isOK()) {
                LOG.error("Status of SaldoReq+KUmsAll+DauerSEPAList batch job not OK " + status);
            }

            if (bookingsJob.getJobResult().getJobStatus().hasErrors()) {
                LOG.error("Bookings job not OK ");
                throw new HBCI_Exception(bookingsJob.getJobResult().getJobStatus().getErrorString());
            }

            bankAccess.setHbciPassportState(new HbciPassport.State(dialog.getPassport()).toJson());

            List<Booking> bookings = HbciMapping.createBookings((GVRKUms) bookingsJob.getJobResult());

            List<StandingOrder> standingOrders = null;
            if (standingOrdersJob != null) {
                standingOrders = HbciMapping.createStandingOrders((GVRDauerList) standingOrdersJob.getJobResult());
            }

            ArrayList<Booking> bookingList = bookings.stream()
                    .collect(Collectors.collectingAndThen(Collectors.toCollection(
                            () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));

            bookingList.forEach(booking -> {
                booking.setCreditorId((Utils.extractCreditorId(booking.getUsage())));
                booking.setMandateReference(Utils.extractMandateReference(booking.getUsage()));
            });

            updateTanTransportTypes(bankAccess, ((HbciPassport) dialog.getPassport()));

            return LoadBookingsResponse.builder()
                    .bookings(bookingList)
                    .bankAccountBalance(HbciMapping.createBalance((GVRSaldoReq) balanceJob.getJobResult()))
                    .standingOrders(standingOrders)
                    .build();
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public void createPayment(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin, Payment payment) {
        HbciTanSubmit hbciTanSubmit = new HbciTanSubmit();

        HBCIDialog dialog = createDialog(bankAccess, bankCode, new HbciCallback() {

            @Override
            public String selectTanMedia(List<GVRTANMediaList.TANMediaInfo> mediaList) {
                return payment.getTanMedia().getMedium();
            }

            @Override
            public void tanChallengeCallback(String challenge) {
                //needed later for submit
                if (challenge != null) {
                    payment.setPaymentChallenge(PaymentChallenge.builder()
                            .title(challenge)
                            .build());
                }
            }

        }, pin);

        HashMap<String, String> secmecParams = dialog.getPassport().getTwostepMechanisms().get(payment.getTanMedia().getId());
        if (secmecParams == null) {
            throw new PaymentException("inavalid tan media: " + payment.getTanMedia().getId());
        }
        dialog.getPassport().setCurrentSecMechInfo(secmecParams);

        try {
            Konto src = dialog.getPassport().getAccount(bankAccount.getAccountNumber());
            src.iban = bankAccount.getIban();
            src.bic = bankAccount.getBic();

            Konto dst = new Konto();
            dst.name = payment.getReceiver();
            dst.iban = payment.getReceiverIban();
            dst.bic = payment.getReceiverBic();

            AbstractHBCIJob uebSEPA = newJob("UebSEPA", dialog.getPassport());
            uebSEPA.setParam("src", src);
            uebSEPA.setParam("dst", dst);
            uebSEPA.setParam("btg", new Value(payment.getAmount()));
            uebSEPA.setParam("usage", payment.getPurpose());
            dialog.addTask(uebSEPA);

            HBCIExecStatus status = dialog.execute(true);
            if (!status.isOK()) {
                throw new PaymentException(status.getDialogStatus().getErrorString());
            }

            bankAccess.setHbciPassportState(new HbciPassport.State(dialog.getPassport()).toJson());

            hbciTanSubmit.setPassportState(bankAccess.getHbciPassportState());
            hbciTanSubmit.setDialogId(dialog.getDialogID());
            hbciTanSubmit.setMsgNum(dialog.getMsgnum());
            payment.setTanSubmitExternal(hbciTanSubmit);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    public void submitPayment(Payment payment, String tan) {
        HbciTanSubmit hbciTanSubmit = (HbciTanSubmit) payment.getTanSubmitExternal();

        HbciPassport.State state = HbciPassport.State.readJson(hbciTanSubmit.getPassportState());
        HbciPassport hbciPassport = createPassport(state.hbciVersion, state.blz, state.customerId, state.userId, new HbciCallback() {

            @Override
            public String needTankCallback() {
                return tan;
            }
        });
        state.apply(hbciPassport);

        HBCIDialog hbciDialog = new HBCIDialog(hbciPassport, hbciTanSubmit.getDialogId(), hbciTanSubmit.getMsgNum());

        try {
            GVTAN2Step hktan = (GVTAN2Step) newJob("TAN2Step", hbciDialog.getPassport());
            hktan.setLowlevelParams(OBJECT_MAPPER.readValue(hbciTanSubmit.getGvTanSubmit().getProperties(), HashMap.class));
            hbciDialog.getMessages().get(hbciDialog.getMessages().size() - 1).add(hktan);

        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean bankSupported(String bankCode) {
        org.kapott.hbci.manager.BankInfo bankInfo = HBCIUtils.getBankInfo(bankCode);
        return bankInfo != null && bankInfo.getPinTanVersion() != null;
    }

    private void updateTanTransportTypes(BankAccess bankAccess, HbciPassport hbciPassport) {
        if (bankAccess.getTanTransportTypes() == null) {
            bankAccess.setTanTransportTypes(new HashMap<>());
        }
        bankAccess.getTanTransportTypes().put(bankApi(), new ArrayList<>());

        if (hbciPassport.getUPD() != null && hbciPassport.getTanMedias() != null) {
            hbciPassport.getAllowedTwostepMechanisms().forEach(id -> {
                HashMap<String, String> properties = hbciPassport.getTwostepMechanisms().get(id);

                if (properties != null) {
                    String name = properties.get("name");
                    bankAccess.getTanTransportTypes().get(bankApi()).add(
                            TanTransportType.builder()
                                    .id(id)
                                    .name(name)
                                    .inputInfo(properties.get("inputinfo"))
                                    .medium(hbciPassport.getTanMedia(name).mediaName)
                                    .build()
                    );
                } else {
                    LOG.warn("unable find transport type {} for bank code {}", id, bankAccess.getBankCode());
                }
            });
        } else {
            LOG.warn("missing passport upd, unable find transport types or bank code {}", bankAccess.getBankCode());
        }
    }

    private HBCIDialog createDialog(BankAccess bankAccess, String bankCode, HbciCallback callback, String pin) {
        return createDialog(null, bankAccess, bankCode, callback, pin);
    }

    private HBCIDialog createDialog(HbciPassport passport, BankAccess bankAccess, String bankCode, HbciCallback callback, String pin) {
        BankInfo bankInfo = HBCIUtils.getBankInfo(bankCode != null ? bankCode : bankAccess.getBankCode());
        bankCode = bankCode != null ? bankCode : bankAccess.getBankCode();

        if (passport == null) {
            passport = createPassport(bankInfo.getPinTanVersion().getId(), bankCode, bankAccess.getBankLogin(), bankAccess.getBankLogin2(), callback);
            if (bankAccess.getHbciPassportState() != null) {
                HbciPassport.State.readJson(bankAccess.getHbciPassportState()).apply(passport);
            }
        }

        passport.setPIN(pin);

        String url = bankInfo.getPinTanAddress();
        String proxyPrefix = System.getProperty("proxyPrefix", null);
        if (proxyPrefix != null) {
            url = proxyPrefix + url;
        }
        passport.setHost(url);

        return new HBCIDialog(passport);
    }

    private HbciPassport createPassport(String hbciVersion, String bankCode, String login, String login2, HbciCallback callback) {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("kernel.rewriter", "InvalidSegment,WrongStatusSegOrder,WrongSequenceNumbers,MissingMsgRef,HBCIVersion,SigIdLeadingZero,InvalidSuppHBCIVersion,SecTypeTAN,KUmsDelimiters,KUmsEmptyBDateSets");
        properties.put("log.loglevel.default", "2");
        properties.put("default.hbciversion", "FinTS3");
        properties.put("client.passport.PinTan.checkcert", "1");
        properties.put("client.passport.PinTan.init", "1");
        properties.put("client.errors.ignoreJobNotSupported", "yes");

        properties.put("client.passport.country", "DE");
        properties.put("client.passport.blz", bankCode);
        properties.put("client.passport.customerId", login);
        properties.put("client.errors.ignoreCryptErrors", "yes");

        if (StringUtils.isNotBlank(login2)) {
            properties.put("client.passport.userId", login2);
        }

        return new HbciPassport(hbciVersion, properties, callback);
    }

    private RuntimeException handleHbciException(HBCI_Exception e) {
        Throwable processException = e;
        while (processException.getCause() != null && !(processException.getCause() instanceof InvalidPinException)) {
            processException = processException.getCause();
        }

        if (processException.getCause() != null && processException.getCause() instanceof InvalidPinException) {
            return (InvalidPinException) processException.getCause();
        }

        return e;
    }


}
