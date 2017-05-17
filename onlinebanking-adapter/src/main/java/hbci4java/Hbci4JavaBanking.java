package hbci4java;

import domain.*;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Hbci4JavaBanking implements OnlineBankingService {

    private static final Logger LOG = LoggerFactory.getLogger(Hbci4JavaBanking.class);
    
    public Hbci4JavaBanking() {
        try {
            HBCIUtils.refreshBLZList(HBCIUtils.class.getClassLoader().getResource("blz.properties").openStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BankApi bankApiIdentifier() {
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
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String pin) {
        LOG.info("Loading Account list for access {}", bankAccess.getBankCode());
        HbciPassport hbciPassport = createPassport(bankAccess, pin);
        HBCIHandler handle = new HBCIHandler(hbciPassport.getHBCIVersion(), hbciPassport);
        try {
            bankAccess.setBankName(hbciPassport.getInstName());
            List<BankAccount> hbciAccounts = new ArrayList<>();
            for (Konto konto : hbciPassport.getAccounts()) {
                hbciAccounts.add(HbciFactory.toBankAccount(konto));
            }
            if (hbciPassport.getState().isPresent()) {
                bankAccess.setPassportState(hbciPassport.getState().get().toJson());
            }
            handle.close();
            return hbciAccounts;
        } catch (HBCI_Exception e) {
            throw new RuntimeException(e);
        } finally {
            handle.close();
        }
    }

    @Override
    public List<Booking> loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, BankAccount bankAccount, String pin) {
        HbciPassport hbciPassport = createPassport(bankAccess, pin);
        HBCIHandler handle = new HBCIHandler(hbciPassport.getHBCIVersion(), hbciPassport);
        try {
            Konto account = hbciPassport.getAccount(bankAccount.getNumberHbciAccount());
            HBCIJob balanceJob = handle.newJob("SaldoReq");
            balanceJob.setParam("my", account);
            balanceJob.addToQueue();
            HBCIJob bookingsJob = handle.newJob("KUmsAll");
            bookingsJob.setParam("my", account);
            bookingsJob.addToQueue();

            // Let the Handler execute all jobs in one batch
            HBCIExecStatus status = handle.execute();
            if (!status.isOK()) {
                LOG.error("Status of SaldoReq+KUmsAll batch job not OK " + status);
            }

            if (hbciPassport.getState().isPresent()) {
                bankAccess.setPassportState(hbciPassport.getState().get().toJson());
            }
            bankAccount.setBankAccountBalance(HbciFactory.createBalance((GVRSaldoReq)balanceJob.getJobResult()));

            return HbciFactory.createBookings((GVRKUms) bookingsJob.getJobResult());
        } catch (HBCI_Exception e) {
            throw new RuntimeException(e);
        } finally {
            handle.close();
        }
    }

    @Override
    public boolean bankSupported(String bankCode) {
        BankInfo bankInfo = HBCIUtils.getBankInfo(bankCode);
        if (bankInfo == null || bankInfo.getPinTanVersion() == null) {
            return false;
        }
        return true;

    }

    private HbciPassport createPassport(BankAccess bankAccess, String pin) {
        Properties properties = new Properties();
        properties.put("kernel.rewriter", "InvalidSegment,WrongStatusSegOrder,WrongSequenceNumbers,MissingMsgRef,HBCIVersion,SigIdLeadingZero,InvalidSuppHBCIVersion,SecTypeTAN,KUmsDelimiters,KUmsEmptyBDateSets");
        properties.put("log.loglevel.default", "2");
        properties.put("default.hbciversion", "FinTS3");
        properties.put("client.passport.PinTan.checkcert", "1");
        properties.put("client.passport.PinTan.init", "1");

        properties.put("client.passport.country", "DE");
        properties.put("client.passport.blz", bankAccess.getBankCode());
        properties.put("client.passport.customerId", bankAccess.getBankLogin());

        HbciPassport passport = new HbciPassport(bankAccess.getPassportState(), properties, null);
        passport.setPIN(pin);

        return passport;
    }


}