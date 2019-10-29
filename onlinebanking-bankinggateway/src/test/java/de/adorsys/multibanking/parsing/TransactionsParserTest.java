package de.adorsys.multibanking.parsing;

import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class TransactionsParserTest {

    @Test
    public void testCamt() throws Exception {
        String camt = IOUtils.toString(TransactionsParserTest.class.getResourceAsStream("/camt.xml"));
        LoadBookingsResponse loadBookingsResponse = TransactionsParser.camtStringToLoadBookingsResponse(camt);
        assertNotNull(loadBookingsResponse);
        assertEquals("Wrong count of bookings", 4, loadBookingsResponse.getBookings().size());
        assertEquals("Wrong balance", BigDecimal.valueOf(123), loadBookingsResponse.getBalancesReport().getReadyBalance().getAmount());
    }

    @Test
    public void testMt940() throws Exception {
        String mt940 = IOUtils.toString(TransactionsParserTest.class.getResourceAsStream("/mt940.txt"));
        LoadBookingsResponse loadBookingsResponse = TransactionsParser.mt940StringToLoadBookingsResponse(mt940);
        assertNotNull(loadBookingsResponse);
        assertEquals("Wrong count of bookings", 5, loadBookingsResponse.getBookings().size());
        assertEquals("Wrong balance", BigDecimal.valueOf(3507505.87), loadBookingsResponse.getBalancesReport().getReadyBalance().getAmount());
    }
}
