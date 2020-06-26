package de.adorsys.multibanking.service;

import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class BookingServiceTest {
    @InjectMocks
    private BookingService bookingService;

    @InjectMocks
    private BankingGatewayAdapter bankingGatewayAdapter;

    @Test
    public void testMerge() throws Exception {
        TransactionsResponse transactionsResponse = bankingGatewayAdapter.jsonStringToLoadBookingsResponse(
            IOUtils.toString(BookingServiceTest.class.getResourceAsStream("/transactions.json"))
        );
        List<BookingEntity> newBookingEntities = bookingService.mapBookings(new BankAccountEntity(), transactionsResponse.getBookings());
        bookingService.mergeBookings(Collections.emptyList(), newBookingEntities);
    }
}
