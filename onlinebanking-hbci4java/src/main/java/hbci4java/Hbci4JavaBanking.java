package hbci4java;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import domain.*;
import exception.InvalidPinException;
import exception.PaymentException;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCIJobFactory;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
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
        HBCIDialog dialog = createDialog(bankAccess, bankCode, null, pin);

        try {
            bankAccess.setBankName(dialog.getPassport().getInstName());
            List<BankAccount> hbciAccounts = new ArrayList<>();
            for (Konto konto : dialog.getPassport().getAccounts()) {
                BankAccount bankAccount = HbciMapping.toBankAccount(konto);
                bankAccount.externalId(bankApi(), UUID.randomUUID().toString());
                bankAccount.bankName(bankAccess.getBankName());
                hbciAccounts.add(bankAccount);
            }
            if (((HbciPassport) dialog.getPassport()).getState().isPresent()) {
                bankAccess.setHbciPassportState(((HbciPassport) dialog.getPassport()).getState().get().toJson());
            }

            updateTanTransportTypes(bankAccess, ((HbciPassport) dialog.getPassport()));

            return hbciAccounts;
        } catch (HBCI_Exception e) {
            handleHbciException(e);
            return null;
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

            AbstractHBCIJob balanceJob = HBCIJobFactory.newJob("SaldoReq", dialog.getPassport(), dialog.getKernel().getMsgGen());
            balanceJob.setParam("my", account);
            dialog.addTask(balanceJob);

            AbstractHBCIJob bookingsJob = HBCIJobFactory.newJob("KUmsAll", dialog.getPassport(), dialog.getKernel().getMsgGen());
            bookingsJob.setParam("my", account);
            if (bankAccount.getLastSync() != null) {
                bookingsJob.setParam("startdate", Date.from(bankAccount.getLastSync().atZone(ZoneId.systemDefault()).toInstant()));
            }
            dialog.addTask(bookingsJob);

            AbstractHBCIJob standingOrdersJob = HBCIJobFactory.newJob("DauerSEPAList", dialog.getPassport(), dialog.getKernel().getMsgGen());
            standingOrdersJob.setParam("src", account);
            if (((AbstractHBCIJob) standingOrdersJob).getHBCICode() == null) {
                LOG.warn("GV DauerSEPAList not supported");
            } else {
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

            if (((HbciPassport) dialog.getPassport()).getState().isPresent()) {
                bankAccess.setHbciPassportState(((HbciPassport) dialog.getPassport()).getState().get().toJson());
            }

            List<Booking> bookings = HbciMapping.createBookings((GVRKUms) bookingsJob.getJobResult());

            List<StandingOrder> standingOrders = HbciMapping.createStandingOrders((GVRDauerList) standingOrdersJob.getJobResult());

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
            handleHbciException(e);
            return null;
        }
    }

    @Override
    public void createPayment(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin, Payment payment) {
        HbciTanSubmit hbciTanSubmit = HbciTanSubmit.builder().build();

        HBCIDialog dialog = createDialog(bankAccess, bankCode, new HbciCallback() {

            @Override
            public boolean tanCallback(HBCIPassport passport, GVTAN2Step hktan) {
                return updateTanSubmit((HbciPassport) passport, hktan, hbciTanSubmit, payment);
            }

        }, pin);

        try {
            Konto src = dialog.getPassport().getAccount(bankAccount.getAccountNumber());

            Konto dst = new Konto();
            dst.name = payment.getReceiver();
            dst.bic = payment.getReceiverBic();
            dst.iban = payment.getReceiverIban();

            Value value = new Value(payment.getAmount());

            AbstractHBCIJob uebSEPA = HBCIJobFactory.newJob("UebSEPA", dialog.getPassport(), dialog.getKernel().getMsgGen());
            uebSEPA.setParam("src", src);
            uebSEPA.setParam("dst", dst);
            uebSEPA.setParam("btg", value);
            uebSEPA.setParam("usage", payment.getPurpose());
            dialog.addTask(uebSEPA);

            HBCIExecStatus status = dialog.execute(true);
            if (!status.isOK()) {
                throw new PaymentException(status.getDialogStatus().getErrorString());
            }

            if (((HbciPassport) dialog.getPassport()).getState().isPresent()) {
                bankAccess.setHbciPassportState(((HbciPassport) dialog.getPassport()).getState().get().toJson());
            }

            hbciTanSubmit.setDialogId(dialog.getDialogID());
            hbciTanSubmit.setMsgNum(dialog.getMsgnum());
            payment.setTanSubmitExternal(hbciTanSubmit);
        } catch (HBCI_Exception e) {
            handleHbciException(e);
        }
    }

    private boolean updateTanSubmit(HbciPassport passport, GVTAN2Step hktan, HbciTanSubmit hbciTanSubmit, Payment payment) {
        try {
            hbciTanSubmit.setGvTanSubmit(HbciGVTanSubmit.builder()
                    .properties(OBJECT_MAPPER.writeValueAsString(hktan.getLowlevelParams()))
                    .build());

            hbciTanSubmit.setHbciPassport(OBJECT_MAPPER.writeValueAsString(passport.clone()));

            Object pintan_challenge = passport.getPersistentData("pintan_challenge");
            if (pintan_challenge != null) {
                payment.setPaymentChallenge(PaymentChallenge.builder().title(pintan_challenge.toString()).build());
            }

            return true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void submitPayment(Payment payment, String tan) {
        HbciTanSubmit hbciTanSubmit = (HbciTanSubmit) payment.getTanSubmitExternal();

        HbciPassport hbciPassport;
        try {
            hbciPassport = OBJECT_MAPPER.readValue(hbciTanSubmit.getHbciPassport(), HbciPassport.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        hbciPassport.setCallback(new HbciCallback());
        hbciPassport.getUPD().put("_fetchedMetaInfo", new Date());

        HBCIDialog hbciDialog = new HBCIDialog(hbciPassport);
        hbciDialog.setDialogid(hbciTanSubmit.getDialogId());
        hbciDialog.setMsgnum(hbciTanSubmit.getMsgNum());

        try {
            GVTAN2Step hktan = (GVTAN2Step) HBCIJobFactory.newJob("TAN2Step", hbciDialog.getPassport(), hbciDialog.getKernel().getMsgGen());
            hktan.setLowlevelParams(OBJECT_MAPPER.readValue(hbciTanSubmit.getGvTanSubmit().getProperties(), Properties.class));
            hbciDialog.getMessages().get(hbciDialog.getMessages().size() - 1).add(hktan);
//                       hktan.getMainPassport().setCallback(new HbciCallback() {
//
//                @Override
//                public void callback(int reason, String msg, int datatype, StringBuffer retData) {
//                    if (reason == NEED_PT_TAN) {
//                        retData.append(tan);
//                    } else {
//                        super.callback(reason, msg, datatype, retData);
//                    }
//                }
//
//            });

//            HBCIExecStatus status = hktan.getParentHandler().execute(true);
//            if (!status.isOK()) {
//                throw new PaymentException(status.getDialogStatus().getErrorString());
//            }
        } catch (HBCI_Exception e) {
            handleHbciException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean bankSupported(String bankCode) {
        org.kapott.hbci.manager.BankInfo bankInfo = HBCIUtils.getBankInfo(bankCode);
        if (bankInfo == null || bankInfo.getPinTanVersion() == null) {
            return false;
        }
        return true;
    }

    private void updateTanTransportTypes(BankAccess bankAccess, HbciPassport hbciPassport) {
        if (bankAccess.getTanTransportTypes() == null) {
            bankAccess.setTanTransportTypes(new HashMap<>());
        }
        bankAccess.getTanTransportTypes().put(bankApi(), new ArrayList<>());

        if (hbciPassport.getUPD() != null) {
            hbciPassport.getAllowedTwostepMechanisms().forEach(id -> {
                Properties properties = hbciPassport.getTwostepMechanisms().get(id);

                if (properties != null) {
                    bankAccess.getTanTransportTypes().get(bankApi()).add(
                            TanTransportType.builder()
                                    .id(id)
                                    .name(properties.getProperty("name") != null ? properties.getProperty("name") : null)
                                    .medium(hbciPassport.getUPD().getProperty("tanmedia.names"))
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
        Properties properties = new Properties();
        properties.put("kernel.rewriter", "InvalidSegment,WrongStatusSegOrder,WrongSequenceNumbers,MissingMsgRef,HBCIVersion,SigIdLeadingZero,InvalidSuppHBCIVersion,SecTypeTAN,KUmsDelimiters,KUmsEmptyBDateSets");
        properties.put("log.loglevel.default", "2");
        properties.put("default.hbciversion", "FinTS3");
        properties.put("client.passport.PinTan.checkcert", "1");
        properties.put("client.passport.PinTan.init", "1");
        properties.put("client.errors.ignoreJobNotSupported", "yes");

        properties.put("client.passport.country", "DE");
        properties.put("client.passport.blz", bankCode != null ? bankCode : bankAccess.getBankCode());
        properties.put("client.passport.customerId", bankAccess.getBankLogin());
        properties.put("client.errors.ignoreCryptErrors", "yes");

        if (StringUtils.isNotBlank(bankAccess.getBankLogin2())) {
            properties.put("client.passport.userId", bankAccess.getBankLogin2());
        }

        HbciPassport passport = new HbciPassport(bankAccess.getHbciPassportState(), properties, callback);
        passport.setPIN(pin);

        return new HBCIDialog(passport);
    }

    private void handleHbciException(HBCI_Exception e) throws InvalidPinException {
        Throwable processException = e;
        while (processException.getCause() != null && !(processException.getCause() instanceof InvalidPinException)) {
            processException = processException.getCause();
        }

        if (processException.getCause() != null && processException.getCause() instanceof InvalidPinException) {
            throw (InvalidPinException) processException.getCause();
        }

        throw e;
    }


}
