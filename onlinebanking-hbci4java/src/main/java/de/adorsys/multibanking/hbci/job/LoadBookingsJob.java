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

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.exception.Message;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.domain.transaction.LoadBookings;
import de.adorsys.multibanking.hbci.model.HbciMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.*;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;
import static de.adorsys.multibanking.domain.transaction.LoadBookings.RawResponseType.CAMT;

@RequiredArgsConstructor
@Slf4j
public class LoadBookingsJob extends ScaRequiredJob<LoadBookingsResponse> {

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
    TransactionRequest getTransactionRequest() {
        return loadBookingsRequest;
    }

    @Override
    String getHbciJobName(AbstractScaTransaction.TransactionType transactionType) {
        return Optional.ofNullable(loadBookingsRequest.getTransaction().getRawResponseType())
            .map(rawResponseType -> {
                if (rawResponseType == CAMT) {
                    return GVKUmsAllCamt.getLowlevelName();
                } else {
                    return GVKUmsAll.getLowlevelName();
                }
            })
            .orElse(GVKUmsAll.getLowlevelName());
    }

    @Override
    BankAccount getPsuBankAccount() {
        return loadBookingsRequest.getTransaction().getPsuAccount();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    public LoadBookingsResponse createJobResponse(PinTanPassport passport) {
        if (bookingsJob.getJobResult().getJobStatus().hasErrors()) {
            log.error("Bookings job not OK");
            throw new MultibankingException(HBCI_ERROR, bookingsJob.getJobResult().getJobStatus().getErrorList().stream()
                .map(messageString -> Message.builder().renderedMessage(messageString).build())
                .collect(Collectors.toList()));
        }

        ArrayList<Booking> bookingList = null;
        List<String> raw = null;
        GVRKUms bookingsResult = (GVRKUms) bookingsJob.getJobResult();
        if (loadBookingsRequest.getTransaction().getRawResponseType() != null) {
            raw = bookingsResult.getRaw();
        } else {
            bookingList = HbciMapping.createBookings(bookingsResult).stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                    () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));
        }

        return LoadBookingsResponse.builder()
            .bookings(bookingList)
            .rawData(raw)
            .build();
    }

    private AbstractHBCIJob createBookingsJob(PinTanPassport passport) {
        AbstractHBCIJob hbciJob = Optional.ofNullable(loadBookingsRequest.getTransaction().getRawResponseType())
            .map(rawResponseType -> {
                if (rawResponseType == CAMT) {
                    return new GVKUmsAllCamt(passport, true);
                } else {
                    return new GVKUmsAll(passport);
                }
            })
            .orElseGet(() -> new GVKUmsAll(passport));

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
