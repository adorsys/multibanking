/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hbci4java.job;

import domain.*;
import domain.request.LoadBookingsRequest;
import domain.response.LoadBookingsResponse;
import hbci4java.model.HbciDialogRequest;
import hbci4java.model.HbciMapping;
import hbci4java.model.HbciPassport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVDauerSEPAList;
import org.kapott.hbci.GV.GVKUmsAll;
import org.kapott.hbci.GV.GVSaldoReq;
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
import java.util.stream.Stream;

import static hbci4java.job.AccountInformationJob.extractTanTransportTypes;
import static hbci4java.model.HbciDialogFactory.createDialog;

@Slf4j
public class LoadBookingsJob {

    public static LoadBookingsResponse loadBookings(LoadBookingsRequest loadBookingsRequest) {
        HbciDialogRequest dialogRequest = HbciDialogRequest.builder()
                .bankCode(loadBookingsRequest.getBankCode() != null ? loadBookingsRequest.getBankCode() :
                        loadBookingsRequest.getBankAccess().getBankCode())
                .customerId(loadBookingsRequest.getBankAccess().getBankLogin())
                .login(loadBookingsRequest.getBankAccess().getBankLogin2())
                .hbciPassportState(loadBookingsRequest.getBankAccess().getHbciPassportState())
                .pin(loadBookingsRequest.getPin())
                .build();

        dialogRequest.setHbciProduct(Optional.ofNullable(loadBookingsRequest.getHbciProduct())
                .map(product -> new HBCIProduct(product.getProduct(), product.getVersion()))
                .orElse(null));
        dialogRequest.setBpd(loadBookingsRequest.getBpd());

        HBCIDialog dialog = createDialog(null, dialogRequest);

        Konto account = createAccount(dialog, loadBookingsRequest.getBankAccount());

        AbstractHBCIJob bookingsJob = createBookingsJob(dialog, loadBookingsRequest, account);

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

        BalancesReport bankAccountBalance = balanceJob
                .map(abstractHBCIJob -> HbciMapping.createBalance((GVRSaldoReq) abstractHBCIJob.getJobResult()))
                .orElse(null);

        ArrayList<Booking> bookingList = null;
        String raw = null;
        GVRKUms bookingsResult = (GVRKUms) bookingsJob.getJobResult();
        if (loadBookingsRequest.isRaw()) {
            raw = bookingsResult.getRaw();
        } else {
            bookingList = HbciMapping.createBookings(bookingsResult).stream()
                    .collect(Collectors.collectingAndThen(Collectors.toCollection(
                            () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));
        }

        if (loadBookingsRequest.isWithTanTransportTypes()) {
            extractTanTransportTypes(dialog.getPassport()).ifPresent(tanTransportTypes -> {
                if (loadBookingsRequest.getBankAccess().getTanTransportTypes() == null) {
                    loadBookingsRequest.getBankAccess().setTanTransportTypes(new HashMap<>());
                }
                loadBookingsRequest.getBankAccess().getTanTransportTypes().put(BankApi.HBCI, tanTransportTypes);
            });
        }

        return LoadBookingsResponse.builder()
                .hbciPassportState(new HbciPassport.State(dialog.getPassport()).toJson())
                .bookings(bookingList)
                .rawData(raw)
                .bankAccountBalance(bankAccountBalance)
                .standingOrders(standingOrders)
                .build();

    }

    private static AbstractHBCIJob createStandingOrdersJob(HBCIDialog dialog, Konto account) {
        AbstractHBCIJob standingOrdersJob = null;
        if (dialog.getPassport().jobSupported("DauerSEPAList")) {
            standingOrdersJob = new GVDauerSEPAList(dialog.getPassport());
            standingOrdersJob.setParam("src", account);
            dialog.addTask(standingOrdersJob);
        }
        return standingOrdersJob;
    }

    private static AbstractHBCIJob createBookingsJob(HBCIDialog dialog, LoadBookingsRequest loadBookingsRequest,
                                                     Konto account) {
        AbstractHBCIJob bookingsJob = new GVKUmsAll(dialog.getPassport());
        bookingsJob.setParam("my", account);

        Optional.ofNullable(loadBookingsRequest.getDateFrom())
                .ifPresent(localDate -> bookingsJob.setParam("startdate",
                        Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));

        Optional.ofNullable(loadBookingsRequest.getDateTo())
                .ifPresent(localDate -> bookingsJob.setParam("enddate",
                        Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));

        dialog.addTask(bookingsJob);
        return bookingsJob;
    }

    private static AbstractHBCIJob createBalanceJob(HBCIDialog dialog, Konto account) {
        AbstractHBCIJob balanceJob = new GVSaldoReq(dialog.getPassport());
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
