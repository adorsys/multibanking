package hbci4java.job;

import domain.*;
import hbci4java.HbciDialogRequest;
import hbci4java.HbciMapping;
import hbci4java.HbciPassport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hbci4java.HbciDialogFactory.createDialog;
import static hbci4java.job.HbciAccountInformationJob.extractTanTransportTypes;
import static org.kapott.hbci.manager.HBCIJobFactory.newJob;

@Slf4j
public class HbciLoadBookingsJob {

    public static LoadBookingsResponse loadBookings(LoadBookingsRequest loadBookingsRequest) {
        HBCIDialog dialog = createDialog(HbciDialogRequest.builder()
                .bankCode(loadBookingsRequest.getBankCode() != null ? loadBookingsRequest.getBankCode() : loadBookingsRequest.getBankAccess().getBankCode())
                .customerId(loadBookingsRequest.getBankAccess().getBankLogin())
                .login(loadBookingsRequest.getBankAccess().getBankLogin2())
                .hbciPassportState(loadBookingsRequest.getBankAccess().getHbciPassportState())
                .pin(loadBookingsRequest.getPin())
                .build());

        Konto account = createAccount(dialog, loadBookingsRequest.getBankAccount());

        AbstractHBCIJob bookingsJob = createBookingsJob(dialog, loadBookingsRequest.getBankAccount().getLastSync(), account);

        Optional<AbstractHBCIJob> balanceJob = loadBookingsRequest.isWithBalance() ?
                Optional.of(createBalanceJob(dialog, account)) :
                Optional.empty();

        Optional<AbstractHBCIJob> standingOrdersJob = loadBookingsRequest.isWithStandingOrders() ?
                Optional.of(createStandingOrdersJob(dialog, account)) :
                Optional.empty();

        // Let the Handler execute all jobs in one batch
        HBCIExecStatus status = dialog.execute(true);
        if (!status.isOK()) {
            log.error("Status of SaldoReq+KUmsAll+DauerSEPAList batch job not OK " + status);

            if (initFailed(status)) {
                throw new HBCI_Exception(status.getErrorString());
            }
        }

        if (bookingsJob.getJobResult().getJobStatus().hasErrors()) {
            log.error("Bookings job not OK");
            throw new HBCI_Exception(bookingsJob.getJobResult().getJobStatus().getErrorString());
        }

        List<StandingOrder> standingOrders = standingOrdersJob
                .map(abstractHBCIJob -> HbciMapping.createStandingOrders((GVRDauerList) abstractHBCIJob.getJobResult()))
                .orElse(null);

        BankAccountBalance bankAccountBalance = balanceJob
                .map(abstractHBCIJob -> HbciMapping.createBalance((GVRSaldoReq) abstractHBCIJob.getJobResult()))
                .orElse(null);


        List<Booking> bookings = HbciMapping.createBookings((GVRKUms) bookingsJob.getJobResult());
        ArrayList<Booking> bookingList = bookings.stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));

//        if (loadBookingsRequest.isUpdateTanTransportTypes()) {
//            extractTanTransportTypes(dialog.getPassport()).ifPresent(tanTransportTypes -> {
//                if (loadBookingsRequest.getBankAccess().getTanTransportTypes() == null) {
//                    loadBookingsRequest.getBankAccess().setTanTransportTypes(new HashMap<>());
//                }
//                loadBookingsRequest.getBankAccess().getTanTransportTypes().put(BankApi.HBCI, tanTransportTypes);
//            });
//        }

        return LoadBookingsResponse.builder()
                .hbciPassportState(new HbciPassport.State(dialog.getPassport()).toJson())
                .bookings(bookingList)
                .bankAccountBalance(bankAccountBalance)
                .standingOrders(standingOrders)
                .build();

    }

    private static AbstractHBCIJob createStandingOrdersJob(HBCIDialog dialog, Konto account) {
        AbstractHBCIJob standingOrdersJob = null;
        if (dialog.getPassport().jobSupported("DauerSEPAList")) {
            standingOrdersJob = newJob("DauerSEPAList", dialog.getPassport());
            standingOrdersJob.setParam("src", account);
            dialog.addTask(standingOrdersJob);
        }
        return standingOrdersJob;
    }

    private static AbstractHBCIJob createBookingsJob(HBCIDialog dialog, LocalDateTime lastSync, Konto account) {
        AbstractHBCIJob bookingsJob = newJob("KUmsAll", dialog.getPassport());
        bookingsJob.setParam("my", account);

        Optional.ofNullable(lastSync)
                .ifPresent(localDateTime -> bookingsJob.setParam("startdate", Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));

        dialog.addTask(bookingsJob);
        return bookingsJob;
    }

    private static AbstractHBCIJob createBalanceJob(HBCIDialog dialog, Konto account) {
        AbstractHBCIJob balanceJob = newJob("SaldoReq", dialog.getPassport());
        balanceJob.setParam("my", account);
        dialog.addTask(balanceJob);
        return balanceJob;
    }

    private static Konto createAccount(HBCIDialog dialog, BankAccount bankAccount) {
        Konto account = dialog.getPassport().findAccountByAccountNumber(bankAccount.getAccountNumber());
        account.iban = bankAccount.getIban();
        account.bic = bankAccount.getBic();
        return account;
    }

    private static boolean initFailed(HBCIExecStatus status) {
        return Stream.of(StringUtils.split(status.getErrorString(), System.getProperty("line.separator")))
                .anyMatch(line -> line.charAt(0) == '9');
    }
}
