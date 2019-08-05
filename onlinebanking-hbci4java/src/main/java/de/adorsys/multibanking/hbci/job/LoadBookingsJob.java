/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.multibanking.hbci.job;

import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.LoadBookingsRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.domain.transaction.StandingOrder;
import de.adorsys.multibanking.hbci.model.HbciMapping;
import de.adorsys.multibanking.hbci.model.HbciPassport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.*;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;
import static de.adorsys.multibanking.hbci.job.AccountInformationJob.extractTanTransportTypes;

@RequiredArgsConstructor
@Slf4j
public class LoadBookingsJob extends ScaRequiredJob {

    private final LoadBookingsRequest loadBookingsRequest;

    private String hbciPassportState;
    private AbstractHBCIJob bookingsJob;
    private AbstractHBCIJob balanceJob;
    private AbstractHBCIJob standingOrdersJob;

    @Override
    AbstractHBCIJob createHbciJob(PinTanPassport passport) {
        bookingsJob = createBookingsJob(passport);
        return bookingsJob;
    }

    @Override
    void beforeExecute(HBCIDialog dialog) {
        balanceJob = loadBookingsRequest.isWithBalance()
            ? createBalanceJob(dialog)
            : null;

        standingOrdersJob = loadBookingsRequest.isWithStandingOrders()
            ? createStandingOrdersJob(dialog)
            : null;
    }

    @Override
    void afterExecute(HBCIDialog dialog) {
        if (bookingsJob.getJobResult().getJobStatus().hasErrors()) {
            log.error("Bookings job not OK");
            throw new MultibankingException(HBCI_ERROR, bookingsJob.getJobResult().getJobStatus().getErrorList());
        }

        if (loadBookingsRequest.isWithTanTransportTypes()) {
            loadBookingsRequest.getBankAccess().setTanTransportTypes(new HashMap<>());
            loadBookingsRequest.getBankAccess().getTanTransportTypes().put(BankApi.HBCI,
                extractTanTransportTypes(dialog.getPassport()));
        }

        hbciPassportState = new HbciPassport.State(dialog.getPassport()).toJson();
    }

    @Override
    TransactionRequest getTransactionRequest() {
        return loadBookingsRequest;
    }

    @Override
    String getHbciJobName(AbstractScaTransaction.TransactionType transactionType) {
        return GVSEPAInfo.getLowlevelName();
    }

    @Override
    String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    public LoadBookingsResponse createResponse() {
        List<StandingOrder> standingOrders = Optional.ofNullable(standingOrdersJob)
            .map(abstractHBCIJob -> HbciMapping.createStandingOrders((GVRDauerList) abstractHBCIJob.getJobResult()))
            .orElse(null);

        BalancesReport bankAccountBalance = Optional.ofNullable(balanceJob)
            .map(abstractHBCIJob -> HbciMapping.createBalance((GVRSaldoReq) abstractHBCIJob.getJobResult(),
                loadBookingsRequest.getBankAccount().getAccountNumber()))
            .orElse(null);

        ArrayList<Booking> bookingList = null;
        List<String> raw = null;
        GVRKUms bookingsResult = (GVRKUms) bookingsJob.getJobResult();
        if (loadBookingsRequest.getRawResponseType() != null) {
            raw = bookingsResult.getRaw();
        } else {
            bookingList = HbciMapping.createBookings(bookingsResult).stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                    () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));
        }

        return LoadBookingsResponse.builder()
            .hbciPassportState(hbciPassportState)
            .bookings(bookingList)
            .rawData(raw)
            .bankAccountBalance(bankAccountBalance)
            .standingOrders(standingOrders)
            .build();
    }

    private AbstractHBCIJob createStandingOrdersJob(HBCIDialog dialog) {
        AbstractHBCIJob standingOrdersJob = null;
        if (dialog.getPassport().jobSupported("DauerSEPAList")) {
            standingOrdersJob = new GVDauerSEPAList(dialog.getPassport());
            standingOrdersJob.setParam("src", createAccount());
            dialog.addTask(standingOrdersJob);
        }
        return standingOrdersJob;
    }

    private AbstractHBCIJob createBookingsJob(PinTanPassport passport) {
        AbstractHBCIJob bookingsJob = Optional.ofNullable(loadBookingsRequest.getRawResponseType())
            .map(rawResponseType -> {
                if (rawResponseType == LoadBookingsRequest.RawResponseType.CAMT) {
                    return new GVKUmsAllCamt(passport, true);
                } else {
                    return new GVKUmsAll(passport);
                }
            })
            .orElseGet(() -> new GVKUmsAll(passport));

        bookingsJob.setParam("my", createAccount());

        Optional.ofNullable(loadBookingsRequest.getDateFrom())
            .ifPresent(localDate -> bookingsJob.setParam("startdate",
                Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));

        Optional.ofNullable(loadBookingsRequest.getDateTo())
            .ifPresent(localDate -> bookingsJob.setParam("enddate",
                Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));

        return bookingsJob;
    }

    private AbstractHBCIJob createBalanceJob(HBCIDialog dialog) {
        AbstractHBCIJob balanceJob = new GVSaldoReq(dialog.getPassport());
        balanceJob.setParam("my", createAccount());
        dialog.addTask(balanceJob);
        return balanceJob;
    }

    private Konto createAccount() {
        Konto account = new Konto();
        account.bic = loadBookingsRequest.getBankAccount().getBic();
        account.iban = loadBookingsRequest.getBankAccount().getIban();
        account.blz = loadBookingsRequest.getBankAccount().getBlz();
        account.number = loadBookingsRequest.getBankAccount().getAccountNumber();
        return account;
    }
}
