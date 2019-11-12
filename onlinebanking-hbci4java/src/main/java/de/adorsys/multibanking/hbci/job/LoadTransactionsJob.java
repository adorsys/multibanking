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
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.exception.Message;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import de.adorsys.multibanking.domain.transaction.AbstractTransaction;
import de.adorsys.multibanking.domain.transaction.LoadTransactions;
import de.adorsys.multibanking.hbci.model.HbciTanSubmit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVKUmsAll;
import org.kapott.hbci.GV.GVKUmsAllCamt;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIMsgStatus;
import org.kapott.hbci.structures.Saldo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.BOOKINGS_FORMAT_NOT_SUPPORTED;
import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;
import static de.adorsys.multibanking.domain.transaction.LoadTransactions.RawResponseType.CAMT;

@RequiredArgsConstructor
@Slf4j
public class LoadTransactionsJob extends ScaAwareJob<LoadTransactions, TransactionsResponse> {

    private final TransactionRequest<LoadTransactions> loadBookingsRequest;

    private AbstractHBCIJob transactionsHbciJob;

    @Override
    public AbstractHBCIJob createJobMessage(PinTanPassport passport) {
        transactionsHbciJob = createTransactionsJob(passport);
        return transactionsHbciJob;
    }

    @Override
    TransactionRequest<LoadTransactions> getTransactionRequest() {
        return loadBookingsRequest;
    }

    @Override
    String getHbciJobName(AbstractTransaction.TransactionType transactionType) {
        if (transactionsHbciJob instanceof GVKUmsAllCamt) {
            return "KUmsAllCamt";
        }
        return "KUmsAll";
    }

    @Override
    public TransactionsResponse createJobResponse(PinTanPassport passport, HbciTanSubmit tanSubmit,
                                                  List<HBCIMsgStatus> msgStatusList) {
        if (transactionsHbciJob.getJobResult().getJobStatus().hasErrors()) {
            log.error("Bookings job not OK");
            throw new MultibankingException(HBCI_ERROR,
                transactionsHbciJob.getJobResult().getJobStatus().getErrorList().stream()
                    .map(messageString -> Message.builder().renderedMessage(messageString).build())
                    .collect(Collectors.toList()));
        }

        List<Booking> bookingList = null;
        BalancesReport balancesReport = null;
        List<String> raw = null;
        GVRKUms bookingsResult = (GVRKUms) transactionsHbciJob.getJobResult();
        if (loadBookingsRequest.getTransaction().getRawResponseType() != null) {
            raw = bookingsResult.getRaw();
        } else {
            if (loadBookingsRequest.getTransaction().isWithBalance() && !bookingsResult.getDataPerDay().isEmpty()) {
                GVRKUms.BTag lastBoookingDay =
                    bookingsResult.getDataPerDay().get(bookingsResult.getDataPerDay().size() - 1);
                balancesReport = createBalancesReport(lastBoookingDay.end);
            }

            bookingList = accountStatementMapper.createBookings(bookingsResult).stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                    () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));
        }

        return TransactionsResponse.builder()
            .bookings(bookingList)
            .balancesReport(balancesReport)
            .rawData(raw)
            .build();
    }

    private BalancesReport createBalancesReport(Saldo saldo) {
        BalancesReport balancesReport = new BalancesReport();
        balancesReport.setReadyBalance(accountStatementMapper.toBalance(saldo));
        return balancesReport;
    }

    private AbstractHBCIJob createTransactionsJob(PinTanPassport passport) {
        AbstractHBCIJob hbciJob = createBookingsJobInternal(passport);

        hbciJob.setParam("my", getPsuKonto(passport));

        LocalDate dateFrom = Optional.ofNullable(loadBookingsRequest.getTransaction().getDateFrom())
            .orElseGet(() -> getStartDate(passport.getJobRestrictions(hbciJob.getName())));
        hbciJob.setParam("startdate", Date.from(dateFrom.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        Optional.ofNullable(loadBookingsRequest.getTransaction().getDateTo())
            .ifPresent(localDate -> hbciJob.setParam("enddate",
                Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));

        return hbciJob;
    }

    private AbstractHBCIJob createBookingsJobInternal(PinTanPassport passport) {
        LoadTransactions.RawResponseType rawResponseType = loadBookingsRequest.getTransaction().getRawResponseType();
        if (rawResponseType != null && !passport.jobSupported(rawResponseType == CAMT ?
            GVKUmsAllCamt.getLowlevelName() : GVKUmsAll.getLowlevelName())) {
            throw new MultibankingException(BOOKINGS_FORMAT_NOT_SUPPORTED, rawResponseType + " not supported");
        }

        return Optional.ofNullable(rawResponseType)
            .map(format -> {
                if (format == CAMT) {
                    return new GVKUmsAllCamt(passport, true);
                } else {
                    return new GVKUmsAll(passport);
                }
            })
            .orElseGet(() -> {
                if (passport.jobSupported(GVKUmsAllCamt.getLowlevelName())) {
                    return new GVKUmsAllCamt(passport, false);
                } else if (passport.jobSupported(GVKUmsAll.getLowlevelName())) {
                    return new GVKUmsAll(passport);
                } else {
                    throw new MultibankingException(HBCI_ERROR, "transaction jobs not supported");
                }
            });
    }

    private LocalDate getStartDate(Map<String, String> jobRestrictions) {
        String days = jobRestrictions.get("timerange");
        LocalDate date;
        if (days != null && days.length() > 0 && days.matches("[0-9]{1,4}")) {
            date = LocalDate.now().minusDays(Long.parseLong(days));
            log.info("earliest start date according to BPD: " + date.toString());
        } else {
            date = LocalDate.now().minusDays(90);
        }

        return date;
    }

}
