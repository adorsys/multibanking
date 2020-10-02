package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import de.adorsys.multibanking.mapper.TransactionsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Slf4j
public class TransactionsParserTest {
    private final static int MOCK_SERVER_PORT = 12345;

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

    @Test
    public void testJsonPagination() throws Exception {
        Executors.newSingleThreadExecutor().submit(new MockServer());
        String json = IOUtils.toString(TransactionsParserTest.class.getResourceAsStream("/pagination.json"), "UTF-8");
        PaginationResolver.PaginationNextCallParameters params = PaginationResolver.PaginationNextCallParameters.builder()
            .bankCode("00000000")
            .consentId("consentID")
            .resourceId("resourceId")
            .bgSessionData(new BgSessionData())
            .withBalance(true)
            .build();
        TransactionsResponse loadBookingsResponse = new PaginationResolver("http://localhost:" + MOCK_SERVER_PORT)
            .jsonStringToLoadBookingsResponse(json, params);
        assertNotNull(loadBookingsResponse);
        assertEquals("Wrong count of bookings", 12, loadBookingsResponse.getBookings().size());

        //Test correct order and balances
        List<Booking> bookings = loadBookingsResponse.getBookings();
        checkAmountAndBalance(bookings.get(0), 100, 200 );
        checkAmountAndBalance(bookings.get(1), 100, 300 );
        checkAmountAndBalance(bookings.get(2), 150, 450 );
        checkAmountAndBalance(bookings.get(3), 200, 650 );
        checkAmountAndBalance(bookings.get(4), 250, 900 );
        checkAmountAndBalance(bookings.get(5), 300, 1200 );
        checkAmountAndBalance(bookings.get(6), 350, 1550 );
        checkAmountAndBalance(bookings.get(7), 400, 1950 );
        checkAmountAndBalance(bookings.get(8), 450, 2400 );
        checkAmountAndBalance(bookings.get(9), 500, 2900 );
        checkAmountAndBalance(bookings.get(10), 550, 3450 );
        checkAmountAndBalance(bookings.get(11), 500.02, 3950.02 );

        assertEquals("Wrong balance", new BigDecimal("3950.02"), loadBookingsResponse.getBalancesReport().getReadyBalance().getAmount());
    }

    private void checkAmountAndBalance(Booking booking, double amount, double balance) {
        assertEquals("Wrong amount", amount, booking.getAmount().doubleValue(), 0);
        assertEquals("Wrong balance", balance, booking.getBalance().doubleValue(), 0);
    }

    final class MockServer implements Runnable {
        private ServerSocket serverSocket;

        @Override
        public void run() {
            log.info("Starting Mock Server");
            try {
                String paginationDir = MockServer.class.getResource("/pagination").getFile();
                List<Path> responses = Files.list(Paths.get(paginationDir))
                    .filter(Files::isRegularFile)
                    .sorted()
                    .collect(Collectors.toList());

                for (int i = 0; i < responses.size(); i++) {
                    serverSocket = new ServerSocket(MOCK_SERVER_PORT);
                    serverSocket.setSoTimeout(2000);
                    Socket server = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
                    char[] buffer = new char[2048];
                    int charsRead = in.read(buffer);
                    String http = new String(buffer).substring(0, charsRead);
                    log.info("http call: " + http);
                    OutputStream outputStream = server.getOutputStream();
                    IOUtils.write(Files.readAllLines(responses.get(i)).stream().collect(Collectors.joining("\n")), outputStream, "UTF-8");
                    outputStream.write("\r\n\r\n".getBytes());
                    outputStream.flush();
                    server.close();
                    serverSocket.close();
                }
            } catch (Exception e) {
                log.error("Error in Mock Server", e);
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException ioe) {
                    log.error("Cannot close socket", ioe);
                }
            }
        }
    }
}
