package hbci4java;

import domain.BankAccess;
import domain.BankAccount;
import domain.Booking;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public Optional<List<BankAccount>> loadBankAccounts(BankAccess bankAccess, String pin) {
        LOG.info("Loading Account list for access {}", bankAccess);
        HbciPassport hbciPassport = createPassport(bankAccess, pin);
        HBCIHandler handle = new HBCIHandler(hbciPassport.getHBCIVersion(), hbciPassport);
        try {
            List<BankAccount> hbciAccounts = new ArrayList<>();
            for (Konto konto : hbciPassport.getAccounts()) {
                hbciAccounts.add(new BankAccount(konto));
            }
            if (hbciPassport.getState().isPresent()) {
                bankAccess.setPassportState(hbciPassport.getState().get().toJson());
            }
            handle.close();
            return Optional.of(hbciAccounts);
        } catch (HBCI_Exception e) {
            throw new RuntimeException(e);
        } finally {
            handle.close();
        }
    }

    @Override
    public Optional<List<Booking>> loadBookings(BankAccess bankAccess, BankAccount bankAccount, String pin) {
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
            bankAccount.bankAccountBalance(HbciFactory.createBalance((GVRSaldoReq)balanceJob.getJobResult()));

            return Optional.of(HbciFactory.createBookings((GVRKUms) bookingsJob.getJobResult()));
        } catch (HBCI_Exception e) {
            throw new RuntimeException(e);
        } finally {
            handle.close();
        }
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

        HbciPassport passport = new HbciPassport(bankAccess.passportState(), properties, null);
        passport.setPIN(pin);

        return passport;
    }


}