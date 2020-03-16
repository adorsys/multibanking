package de.adorsys.multibanking.mapper;

import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import org.kapott.hbci.GV.parsers.ISEPAParser;
import org.kapott.hbci.GV.parsers.SEPAParserFactory;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.sepa.SepaVersion;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.swift.Swift;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionsParser {

    private TransactionsParser(){}

    private static AccountStatementMapper accountStatementMapper = new AccountStatementMapperImpl();

    @SuppressWarnings("unchecked")
    public static TransactionsResponse camtStringToLoadBookingsResponse(String body) {
        SepaVersion version = SepaVersion.autodetect(body);
        ISEPAParser<List<GVRKUms.BTag>> parser = SEPAParserFactory.get(version);
        GVRKUms bookingsResult = new GVRKUms(null);
        parser.parse(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), bookingsResult.getDataPerDay());
        return jobresultToLoadBookingsResponse(bookingsResult, body);
    }

    public static TransactionsResponse mt940StringToLoadBookingsResponse(String body) {
        GVRKUms bookingsResult = new GVRKUms(null);
        bookingsResult.appendMt940raw(new StringBuilder(Swift.decodeUmlauts(body)));
        return jobresultToLoadBookingsResponse(bookingsResult, body);
    }

    private static TransactionsResponse jobresultToLoadBookingsResponse(GVRKUms bookingsResult, String raw) {
        List<Booking> bookings = accountStatementMapper.createBookings(bookingsResult).stream()
            .collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));

        bookings.forEach(booking -> booking.setBankApi(BankApi.XS2A));

        BalancesReport balancesReport = null;
        if (!bookingsResult.getDataPerDay().isEmpty()) {
            GVRKUms.BTag lastBoookingDay =
                bookingsResult.getDataPerDay().get(bookingsResult.getDataPerDay().size() - 1);

            if (lastBoookingDay.end != null && lastBoookingDay.end.timestamp != null) { // balance is always with date
                balancesReport = createBalancesReport(lastBoookingDay.end);
            }
        }

        return TransactionsResponse.builder()
            .bookings(bookings)
            .balancesReport(balancesReport)
            .rawData(Collections.singletonList(raw))
            .build();
    }

    private static BalancesReport createBalancesReport(Saldo saldo) {
        BalancesReport balancesReport = new BalancesReport();
        balancesReport.setReadyBalance(accountStatementMapper.toBalance(saldo));
        return balancesReport;
    }
}
