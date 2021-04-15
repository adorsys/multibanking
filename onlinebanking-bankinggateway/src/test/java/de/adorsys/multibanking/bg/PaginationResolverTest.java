package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Slf4j
public class PaginationResolverTest {
    private final static int MOCK_SERVER_PORT = 12345;

    @Test
    public void testJsonPaginationClosingBooked() throws Exception {
        Executors.newSingleThreadExecutor().submit(new MockServer("/paginationClosingBooked"));
        TimeUnit.SECONDS.sleep(1); // wait for server

        String json = IOUtils.toString(TransactionsParserTest.class.getResourceAsStream("/pagination.json"), "UTF-8");
        PaginationResolver.PaginationNextCallParameters params = PaginationResolver.PaginationNextCallParameters.builder()
            .bankCode("00000000")
            .consentId("consentID")
            .resourceId("resourceId")
            .bgSessionData(new BgSessionData())
            .dateFrom(LocalDate.now().minusDays(5))
            .dateTo(LocalDate.now())
            .withBalance(true)
            .build();
        TransactionsResponse loadBookingsResponse = new PaginationResolver("http://localhost:" + MOCK_SERVER_PORT)
            .jsonStringToLoadBookingsResponse(json, params);
        assertNotNull(loadBookingsResponse);
        assertEquals("Wrong count of bookings", 12, loadBookingsResponse.getBookings().size());

        //Test correct order and balances
        List<Booking> bookings = loadBookingsResponse.getBookings();
        Collections.reverse(bookings); //last comes first
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

    @Test
    public void testJsonPaginationExpected() throws Exception {
        Executors.newSingleThreadExecutor().submit(new MockServer("/paginationExpected"));
        TimeUnit.SECONDS.sleep(2); // wait for server

        String json = IOUtils.toString(TransactionsParserTest.class.getResourceAsStream("/pagination.json"), "UTF-8");
        PaginationResolver.PaginationNextCallParameters params = PaginationResolver.PaginationNextCallParameters.builder()
            .bankCode("00000000")
            .consentId("consentID")
            .resourceId("resourceId")
            .bgSessionData(new BgSessionData())
            .dateFrom(LocalDate.now().minusDays(5))
            .dateTo(LocalDate.now())
            .withBalance(true)
            .build();
        TransactionsResponse loadBookingsResponse = new PaginationResolver("http://localhost:" + MOCK_SERVER_PORT)
            .jsonStringToLoadBookingsResponse(json, params);
        assertNotNull(loadBookingsResponse);
        assertEquals("Wrong count of bookings", 12, loadBookingsResponse.getBookings().size());

        //Test correct order and balances
        List<Booking> bookings = loadBookingsResponse.getBookings();
        Collections.reverse(bookings); //last comes first
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

        assertEquals("Wrong balance", new BigDecimal("4100.02"), loadBookingsResponse.getBalancesReport().getUnreadyBalance().getAmount());
    }

    private void checkAmountAndBalance(Booking booking, double amount, double balance) {
        assertEquals("Wrong amount", amount, booking.getAmount().doubleValue(), 0);
        assertNotNull("Balance is null", booking.getBalance());
        assertEquals("Wrong balance", balance, booking.getBalance().doubleValue(), 0);
    }

    @RequiredArgsConstructor
    final class MockServer implements Runnable {
        private final String directory;
        private ServerSocket serverSocket;

        @Override
        public void run() {
            log.info("Starting Mock Server");
            try {
                String paginationDir = MockServer.class.getResource(directory).getFile();
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
                    log.info("http response file: " + responses.get(i));
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
