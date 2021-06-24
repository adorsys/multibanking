package de.adorsys.multibanking.bg.resolver;

import de.adorsys.multibanking.domain.response.TransactionsResponse;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class DownloadResolverTest {

    @Ignore("needs xs2a-adapter on 8999 and netcat on 12345 - see src/test/resources/download") // cat camts.http camts.zip | nc -l 12345
    @Test
    public void downloadZipWithXs2aAdapter() throws Exception {
        DownloadResolver downloadResolver = new DownloadResolver("http://localhost:8999");
        TransactionsResponse transactionsResponse = downloadResolver.loadTransactions("http://localhost:12345", "30020900", "egal"); // caution without http:// it goes to targo
        assertEquals("3 camt files expected", 3, transactionsResponse.getRawData().size());
        assertEquals("12 bookings expected", 12, transactionsResponse.getBookings().size());
        assertEquals("balance 123 expected", BigDecimal.valueOf(123), transactionsResponse.getBalancesReport().getReadyBalance().getAmount());
    }

    @Test
    public void readZipFromFilesystem() throws Exception {
        DownloadResolver downloadResolver = new DownloadResolver(null);
        byte [] zip = Files.readAllBytes(Paths.get(DownloadResolverTest.class.getResource("/download/camts.zip").toURI()));
        TransactionsResponse transactionsResponse = downloadResolver.readZip(zip);

        assertEquals("3 camt files expected", 3, transactionsResponse.getRawData().size());
        assertEquals("12 bookings expected", 12, transactionsResponse.getBookings().size());
        assertEquals("balance 123 expected", BigDecimal.valueOf(123), transactionsResponse.getBalancesReport().getReadyBalance().getAmount());
    }
}
