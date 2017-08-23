package hbci4java;

import domain.*;
import exception.InvalidPinException;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.GVDauerSEPAList;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.exceptions.ProcessException;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.status.HBCIMsgStatus;
import org.kapott.hbci.status.HBCIRetVal;
import org.kapott.hbci.structures.Konto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spi.OnlineBankingService;
import utils.Utils;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Hbci4JavaBanking implements OnlineBankingService {

    private static final Logger LOG = LoggerFactory.getLogger(Hbci4JavaBanking.class);

    public Hbci4JavaBanking() {
        try (InputStream inputStream = HBCIUtils.class.getClassLoader().getResource("blz.properties").openStream()) {
            HBCIUtils.refreshBLZList(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BankApi bankApi() {
        return BankApi.HBCI;
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
    public Bank getBankLoginSettings(String bankCode) {
        return null;
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    @Override
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, boolean storePin) {
        LOG.info("Loading Account list for access {}", bankAccess.getBankCode());
        HbciPassport hbciPassport = createPassport(bankAccess, bankCode, pin);
        HBCIHandler handle = null;
        try {
            handle = new HBCIHandler(hbciPassport.getHBCIVersion(), hbciPassport);
            bankAccess.setBankName(hbciPassport.getInstName());
            List<BankAccount> hbciAccounts = new ArrayList<>();
            for (Konto konto : hbciPassport.getAccounts()) {
                BankAccount bankAccount = HbciFactory.toBankAccount(konto);
                bankAccount.externalId(bankApi(), UUID.randomUUID().toString());
                bankAccount.bankName(bankAccess.getBankName());
                hbciAccounts.add(bankAccount);
            }
            if (hbciPassport.getState().isPresent()) {
                bankAccess.setHbciPassportState(hbciPassport.getState().get().toJson());
            }
            handle.close();
            return hbciAccounts;
        } catch (HBCI_Exception e) {
            handleHbciException(e);
            return null;
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //not needed
    }

    @Override
    public List<Booking> loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin) {
        HbciPassport hbciPassport = createPassport(bankAccess, bankCode, pin);
        HBCIHandler handle = new HBCIHandler(hbciPassport.getHBCIVersion(), hbciPassport);
        try {
            Konto account = hbciPassport.getAccount(bankAccount.getAccountNumber());
            HBCIJob balanceJob = handle.newJob("SaldoReq");
            balanceJob.setParam("my", account);
            balanceJob.addToQueue();
            HBCIJob bookingsJob = handle.newJob("KUmsAll");
            bookingsJob.setParam("my", account);
            bookingsJob.addToQueue();
            HBCIJob standingOrdersJob = handle.newJob("DauerSEPAList");
            standingOrdersJob.setParam("src", account);
            standingOrdersJob.addToQueue();

            // Let the Handler execute all jobs in one batch
            HBCIExecStatus status = handle.execute();
            if (!status.isOK()) {
                LOG.error("Status of SaldoReq+KUmsAll batch job not OK " + status);
            }

            if (hbciPassport.getState().isPresent()) {
                bankAccess.setHbciPassportState(hbciPassport.getState().get().toJson());
            }
            bankAccount.setBankAccountBalance(HbciFactory.createBalance((GVRSaldoReq) balanceJob.getJobResult()));

            List<Booking> bookings = HbciFactory.createBookings((GVRKUms) bookingsJob.getJobResult());

            List<StandingOrder> standingOrders = HbciFactory.createStandingOrders((GVRDauerList) standingOrdersJob.getJobResult());

            ArrayList<Booking> bookingList = bookings.stream()
                    .collect(Collectors.collectingAndThen(Collectors.toCollection(
                            () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));

            bookingList.forEach(booking -> booking.setCreditorId((Utils.extractCreditorId(booking.getUsage()))));

            bookingList.forEach(booking ->
                    standingOrders
                            .stream()
                            .filter(so -> so.getAmount().negate().compareTo(booking.getAmount()) == 0 &&
                                    Utils.inCycle(booking.getValutaDate(), so.getExecutionDay()) &&
                                    Utils.usageContains(booking.getUsage(), so.getUsage())
                            )
                            .findFirst()
                            .ifPresent(standingOrder -> {
                                booking.setStandingOrder(true);
                            }));

            return bookingList;
        } catch (HBCI_Exception e) {
            handleHbciException(e);
            return null;
        } finally {
            handle.close();
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

    private HbciPassport createPassport(BankAccess bankAccess, String bankCode, String pin) {
        Properties properties = new Properties();
        properties.put("kernel.rewriter", "InvalidSegment,WrongStatusSegOrder,WrongSequenceNumbers,MissingMsgRef,HBCIVersion,SigIdLeadingZero,InvalidSuppHBCIVersion,SecTypeTAN,KUmsDelimiters,KUmsEmptyBDateSets");
        properties.put("log.loglevel.default", "2");
        properties.put("default.hbciversion", "FinTS3");
        properties.put("client.passport.PinTan.checkcert", "1");
        properties.put("client.passport.PinTan.init", "1");

        properties.put("client.passport.country", "DE");
        properties.put("client.passport.blz", bankCode != null ? bankCode : bankAccess.getBankCode());
        properties.put("client.passport.customerId", bankAccess.getBankLogin());
        if (StringUtils.isNotBlank(bankAccess.getBankLogin2())) {
            properties.put("client.passport.userId", bankAccess.getBankLogin2());
        }

        HbciPassport passport = new HbciPassport(bankAccess.getHbciPassportState(), properties, null);
        passport.setPIN(pin);

        return passport;
    }

    private void handleHbciException(HBCI_Exception e) throws InvalidPinException {
        Throwable processException = e;
        while (processException.getCause() != null && !(processException.getCause() instanceof ProcessException)) {
            processException = processException.getCause();
        }

        if (processException.getCause() != null && processException.getCause() instanceof ProcessException) {
            HBCIMsgStatus msgStatus = ((ProcessException) processException.getCause()).getMsgStatus();
            if (isInvalidPIN(msgStatus)) {
                throw new InvalidPinException();
            }
        }

        throw new RuntimeException(e);
    }

    /**
     * Gibt zurück, ob der Fehler "PIN ungültig" zurückgemeldet wurde
     *
     * @return <code>true</code> oder <code>false</code>
     */
    public boolean isInvalidPIN(HBCIMsgStatus msgStatus) {

        List<HBCIRetVal> retvals = new ArrayList<HBCIRetVal>(Arrays.asList(msgStatus.globStatus.getErrors()));
        retvals.addAll(new ArrayList<>(Arrays.asList(msgStatus.segStatus.getErrors())));

        for (Iterator<HBCIRetVal> i = retvals.iterator(); i.hasNext(); ) {
            HBCIRetVal ret = i.next();

            if (ret.code.equals("9931") || ret.code.equals("9942") ||      // PIN falsch (konkret)
                    ret.code.equals("9340"))    // Signatur falsch (generisch)
            {
                return true;
            }
        }

        return false;
    }

}