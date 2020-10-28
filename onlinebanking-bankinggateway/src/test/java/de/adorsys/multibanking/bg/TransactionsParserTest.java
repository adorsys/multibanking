package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.domain.response.TransactionsResponse;
import de.adorsys.multibanking.mapper.TransactionsParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class TransactionsParserTest {

    @Test
    public void testCamt() throws Exception {
        String camt = IOUtils.toString(TransactionsParserTest.class.getResourceAsStream("/camt.xml"));
        TransactionsResponse loadBookingsResponse = TransactionsParser.camtStringToLoadBookingsResponse(camt);
        assertNotNull(loadBookingsResponse);
        assertEquals("Wrong count of bookings", 4, loadBookingsResponse.getBookings().size());
        assertEquals("Wrong balance", BigDecimal.valueOf(123), loadBookingsResponse.getBalancesReport().getReadyBalance().getAmount());
    }

    @Test
    public void testMt940() throws Exception {
        String mt940 = IOUtils.toString(TransactionsParserTest.class.getResourceAsStream("/mt940.txt"));
        TransactionsResponse loadBookingsResponse = TransactionsParser.mt940StringToLoadBookingsResponse(mt940);
        assertNotNull(loadBookingsResponse);
        assertEquals("Wrong count of bookings", 5, loadBookingsResponse.getBookings().size());
        assertEquals("Wrong balance", BigDecimal.valueOf(3507505.87), loadBookingsResponse.getBalancesReport().getReadyBalance().getAmount());
    }

    @Test
    public void testJson() throws Exception {
        String json = IOUtils.toString(TransactionsParserTest.class.getResourceAsStream("/transactions.json"), "UTF-8");
        TransactionsResponse loadBookingsResponse = new PaginationResolver(null).jsonStringToLoadBookingsResponse(json, null);
        assertNotNull(loadBookingsResponse);
        assertEquals("Wrong count of bookings", 30, loadBookingsResponse.getBookings().size());
        loadBookingsResponse.getBookings().forEach(
            booking -> assertNotNull(booking.getText())
        );
        assertEquals("Buchungstext wrong","Auszahlungen/Bargeldauszahlung", loadBookingsResponse.getBookings().get(0).getText());
    }
}
