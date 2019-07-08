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

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.LoadBookingsRequest;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.hbci.model.HbciCallback;
import de.adorsys.multibanking.hbci.model.HbciDialogRequest;
import de.adorsys.multibanking.hbci.model.HbciMapping;
import de.adorsys.multibanking.hbci.model.HbciPassport;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.*;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;
import static de.adorsys.multibanking.hbci.job.AccountInformationJob.extractTanTransportTypes;
import static de.adorsys.multibanking.hbci.model.HbciDialogFactory.createDialog;

@Slf4j
public class LoadBookingsJob {

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
        AbstractHBCIJob bookingsJob = Optional.ofNullable(loadBookingsRequest.getRawResponseType())
                .map(rawResponseType -> {
                    if (rawResponseType == LoadBookingsRequest.RawResponseType.CAMT) {
                        return new GVKUmsAllCamt(dialog.getPassport(), true);
                    } else {
                        return new GVKUmsAll(dialog.getPassport());
                    }
                })
                .orElseGet(() -> new GVKUmsAll(dialog.getPassport()));

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

    public LoadBookingsResponse loadBookings(LoadBookingsRequest request, HbciCallback callback) {
        HbciDialogRequest dialogRequest = HbciDialogRequest.builder()
                .bankCode(request.getBankCode() != null ? request.getBankCode() :
                        request.getBankAccess().getBankCode())
                .customerId(request.getBankAccess().getBankLogin())
                .login(request.getBankAccess().getBankLogin2())
                .hbciPassportState(request.getBankAccess().getHbciPassportState())
                .pin(request.getPin())
                .callback(callback)
                .build();

        dialogRequest.setProduct(Optional.ofNullable(request.getProduct())
                .map(product -> new Product(product.getName(), product.getVersion()))
                .orElse(null));
        dialogRequest.setBpd(request.getBpd());

        HBCIDialog dialog = createDialog(null, dialogRequest);

        Konto account = createAccount(dialog, request.getBankAccount());

        AbstractHBCIJob bookingsJob = createBookingsJob(dialog, request, account);

        Optional<AbstractHBCIJob> balanceJob = request.isWithBalance() ?
                Optional.of(createBalanceJob(dialog, account)) :
                Optional.empty();

        Optional<AbstractHBCIJob> standingOrdersJob = request.isWithStandingOrders() ?
                Optional.of(createStandingOrdersJob(dialog, account)) :
                Optional.empty();

        // Let the Handler submitAuthorizationCode all jobs in one batch
        HBCIExecStatus dialogStatus = dialog.execute(true);
        if (!dialogStatus.isOK()) {
            log.warn(dialogStatus.getErrorMessages().toString());
        }

        if (bookingsJob.getJobResult().getJobStatus().hasErrors()) {
            log.error("Bookings job not OK");
            throw new MultibankingException(HBCI_ERROR, bookingsJob.getJobResult().getJobStatus().getErrorList());
        }

        List<StandingOrder> standingOrders = standingOrdersJob
                .map(abstractHBCIJob -> HbciMapping.createStandingOrders((GVRDauerList) abstractHBCIJob.getJobResult()))
                .orElse(null);

        BalancesReport bankAccountBalance = balanceJob
                .map(abstractHBCIJob -> HbciMapping.createBalance((GVRSaldoReq) abstractHBCIJob.getJobResult(), account.number))
                .orElse(null);

        ArrayList<Booking> bookingList = null;
        String raw = null;
        GVRKUms bookingsResult = (GVRKUms) bookingsJob.getJobResult();
        if (request.getRawResponseType() != null) {
            raw = bookingsResult.getRaw();
        } else {
            bookingList = HbciMapping.createBookings(bookingsResult).stream()
                    .collect(Collectors.collectingAndThen(Collectors.toCollection(
                            () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));
        }

        if (request.isWithTanTransportTypes()) {
            request.getBankAccess().setTanTransportTypes(new HashMap<>());
            request.getBankAccess().getTanTransportTypes().put(BankApi.HBCI,
                    extractTanTransportTypes(dialog.getPassport()));
        }

        return LoadBookingsResponse.builder()
                .hbciPassportState(new HbciPassport.State(dialog.getPassport()).toJson())
                .bookings(bookingList)
                .rawData(raw)
                .bankAccountBalance(bankAccountBalance)
                .standingOrders(standingOrders)
                .build();

    }
}
