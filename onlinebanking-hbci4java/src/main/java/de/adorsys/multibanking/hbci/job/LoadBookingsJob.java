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
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.domain.transaction.LoadBookings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVKUmsAll;
import org.kapott.hbci.GV.GVKUmsAllCamt;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Saldo;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.BOOKINGS_FORMAT_NOT_SUPPORTED;
import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;
import static de.adorsys.multibanking.domain.transaction.LoadBookings.RawResponseType.CAMT;

@RequiredArgsConstructor
@Slf4j
public class LoadBookingsJob extends ScaRequiredJob<LoadBookings, LoadBookingsResponse> {

    private final TransactionRequest<LoadBookings> loadBookingsRequest;

    private AbstractHBCIJob bookingsJob;

    @Override
    public AbstractHBCIJob createScaMessage(PinTanPassport passport) {
        bookingsJob = createBookingsJob(passport);
        return bookingsJob;
    }

    @Override
    public List<AbstractHBCIJob> createAdditionalMessages(PinTanPassport passport) {
        return Collections.emptyList();
    }

    @Override
    TransactionRequest<LoadBookings> getTransactionRequest() {
        return loadBookingsRequest;
    }

    @Override
    String getHbciJobName(AbstractScaTransaction.TransactionType transactionType) {
        boolean camt = Optional.ofNullable(loadBookingsRequest.getTransaction().getRawResponseType())
            .map(rawResponseType -> rawResponseType == CAMT)
            .orElse(false);

        return camt ? "KUmsAllCamt" : "KUmsAll";
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    public LoadBookingsResponse createJobResponse(PinTanPassport passport, AbstractHBCIJob hbciJob) {
        AbstractHBCIJob resultJob = Optional.ofNullable(hbciJob)
            .orElse(this.bookingsJob);

        if (resultJob.getJobResult().getJobStatus().hasErrors()) {
            log.error("Bookings job not OK");
            throw new MultibankingException(HBCI_ERROR,
                resultJob.getJobResult().getJobStatus().getErrorList().stream()
                    .map(messageString -> Message.builder().renderedMessage(messageString).build())
                    .collect(Collectors.toList()));
        }

        List<Booking> bookingList = null;
        BalancesReport balancesReport = null;
        List<String> raw = null;
        GVRKUms bookingsResult = (GVRKUms) resultJob.getJobResult();
        if (loadBookingsRequest.getTransaction().getRawResponseType() != null) {
            raw = bookingsResult.getRaw();
        } else {
            if (loadBookingsRequest.getTransaction().isWithBalance() && !bookingsResult.getDataPerDay().isEmpty()) {
                GVRKUms.BTag lastBoookingDay =
                    bookingsResult.getDataPerDay().get(bookingsResult.getDataPerDay().size() - 1);
                balancesReport = createBalancesReport(lastBoookingDay.end);
            }

            bookingList = hbciObjectMapper.createBookings(bookingsResult).stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                    () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));
        }

        return LoadBookingsResponse.builder()
            .bookings(bookingList)
            .balancesReport(balancesReport)
            .rawData(raw)
            .build();
    }

    private BalancesReport createBalancesReport(Saldo saldo) {
        BalancesReport balancesReport = new BalancesReport();
        balancesReport.setReadyBalance(hbciObjectMapper.toBalance(saldo));
        return balancesReport;
    }

    private AbstractHBCIJob createBookingsJob(PinTanPassport passport) {
        LoadBookings.RawResponseType rawResponseType = loadBookingsRequest.getTransaction().getRawResponseType();
        if (rawResponseType != null && passport.jobSupported(rawResponseType == CAMT ?
            GVKUmsAllCamt.getLowlevelName() : GVKUmsAll.getLowlevelName())) {
            throw new MultibankingException(BOOKINGS_FORMAT_NOT_SUPPORTED, rawResponseType + " not supported");
        }

        AbstractHBCIJob hbciJob = Optional.ofNullable(rawResponseType)
            .map(format -> {
                if (format == CAMT) {
                    return new GVKUmsAllCamt(passport, true);
                } else {
                    return new GVKUmsAll(passport);
                }
            })
            .orElseGet(() -> new GVKUmsAllCamt(passport, false));

        hbciJob.setParam("my", getPsuKonto(passport));

        Optional.ofNullable(loadBookingsRequest.getTransaction().getDateFrom())
            .ifPresent(localDate -> hbciJob.setParam("startdate",
                Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));

        Optional.ofNullable(loadBookingsRequest.getTransaction().getDateTo())
            .ifPresent(localDate -> hbciJob.setParam("enddate",
                Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));

        return hbciJob;
    }

}
