package hbci4java.job;

import domain.*;
import hbci4java.HbciMapping;
import hbci4java.HbciPassport;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static hbci4java.HbciDialogFactory.createDialog;
import static hbci4java.job.HbciAccountInformationJob.updateTanTransportTypes;
import static org.kapott.hbci.manager.HBCIJobFactory.newJob;

@Slf4j
public class HbciLoadBookingsJob {

    public static LoadBookingsResponse loadBookings(BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin) {
        HBCIDialog dialog = createDialog(bankAccess, bankCode, null, pin);

        Konto account = dialog.getPassport().findAccountByAccountNumber(bankAccount.getAccountNumber());
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
            log.error("Status of SaldoReq+KUmsAll+DauerSEPAList batch job not OK " + status);
        }

        if (bookingsJob.getJobResult().getJobStatus().hasErrors()) {
            log.error("Bookings job not OK");
            throw new HBCI_Exception(bookingsJob.getJobResult().getJobStatus().getErrorString());
        }

        bankAccess.setHbciPassportState(new HbciPassport.State(dialog.getPassport()).toJson());

        List<StandingOrder> standingOrders = null;
        if (standingOrdersJob != null) {
            standingOrders = HbciMapping.createStandingOrders((GVRDauerList) standingOrdersJob.getJobResult());
        }

        List<Booking> bookings = HbciMapping.createBookings((GVRKUms) bookingsJob.getJobResult());
        ArrayList<Booking> bookingList = bookings.stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));

        updateTanTransportTypes(bankAccess, dialog.getPassport());

        return LoadBookingsResponse.builder()
                .bookings(bookingList)
                .bankAccountBalance(HbciMapping.createBalance((GVRSaldoReq) balanceJob.getJobResult()))
                .standingOrders(standingOrders)
                .build();

    }
}
