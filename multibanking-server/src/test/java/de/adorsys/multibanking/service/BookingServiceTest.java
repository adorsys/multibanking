package de.adorsys.multibanking.service;

import de.adorsys.multibanking.bg.PaginationResolver;
import de.adorsys.multibanking.bg.utils.GsonConfig;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import de.adorsys.multibanking.xs2a_adapter.model.TransactionsResponse200Json;
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
    private PaginationResolver bankingGatewayAdapter;

    @Test
    public void testMerge() throws Exception {
        String json =  IOUtils.toString(BookingServiceTest.class.getResourceAsStream("/transactions.json"), "UTF-8");
        TransactionsResponse200Json transactionsResponse200JsonTO = GsonConfig.getGson().fromJson(json, TransactionsResponse200Json.class);
        TransactionsResponse transactionsResponse = bankingGatewayAdapter.toLoadBookingsResponse(transactionsResponse200JsonTO , null);
        List<BookingEntity> newBookingEntities = bookingService.mapBookings(new BankAccountEntity(), transactionsResponse.getBookings());
        bookingService.mergeBookings(Collections.emptyList(), newBookingEntities);
    }
}
