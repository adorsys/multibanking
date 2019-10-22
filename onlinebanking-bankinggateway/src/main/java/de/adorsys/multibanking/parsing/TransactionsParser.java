package de.adorsys.multibanking.parsing;

import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.hbci.model.HbciObjectMapper;
import de.adorsys.multibanking.hbci.model.HbciObjectMapperImpl;
import org.apache.commons.io.IOUtils;
import org.kapott.hbci.GV.parsers.ISEPAParser;
import org.kapott.hbci.GV.parsers.SEPAParserFactory;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.sepa.SepaVersion;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.swift.Swift;

import java.util.*;
import java.util.stream.Collectors;

public class TransactionsParser {
    static HbciObjectMapper hbciObjectMapper = new HbciObjectMapperImpl();

    public static LoadBookingsResponse camtStringToLoadBookingsResponse(String body) {
        SepaVersion version = SepaVersion.autodetect(body);
        ISEPAParser<List<GVRKUms.BTag>> parser = SEPAParserFactory.get(version);
        GVRKUms bookingsResult = new GVRKUms(null);
        parser.parse(IOUtils.toInputStream(body), bookingsResult.getDataPerDay());
        return jobresultToLoadBookingsResponse(bookingsResult, body);
    }

    public static LoadBookingsResponse mt940StringToLoadBookingsResponse(String body) {
        GVRKUms bookingsResult = new GVRKUms(null);
        bookingsResult.appendMt940raw(new StringBuilder(Swift.decodeUmlauts(body)));
        return jobresultToLoadBookingsResponse(bookingsResult, body);
    }

    private static LoadBookingsResponse jobresultToLoadBookingsResponse(GVRKUms bookingsResult, String raw) {
        List<Booking> bookings =  hbciObjectMapper.createBookings(bookingsResult).stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));

        BalancesReport balancesReport = null;
        if (!bookingsResult.getDataPerDay().isEmpty()) {
            GVRKUms.BTag lastBoookingDay =
                    bookingsResult.getDataPerDay().get(bookingsResult.getDataPerDay().size() - 1);

            if (lastBoookingDay.end != null && lastBoookingDay.end.timestamp != null) { // balance is always with date
                balancesReport = createBalancesReport(lastBoookingDay.end);
            }
        }

        return LoadBookingsResponse.builder()
                .bookings(bookings)
                .balancesReport(balancesReport)
                .rawData(Arrays.asList(raw))
                .build();
    }

    private static BalancesReport createBalancesReport(Saldo saldo) {
        BalancesReport balancesReport = new BalancesReport();
        balancesReport.setReadyBalance(hbciObjectMapper.toBalance(saldo));
        return balancesReport;
    }
}
