package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.bg.mapper.BankingGatewayMapper;
import de.adorsys.multibanking.bg.mapper.BankingGatewayMapperImpl;
import de.adorsys.multibanking.bg.resolver.BalanceCalculator;
import de.adorsys.multibanking.bg.resolver.BalanceResolver;
import de.adorsys.multibanking.bg.resolver.PageResolver;
import de.adorsys.multibanking.bg.utils.GsonConfig;
import de.adorsys.multibanking.domain.Balance;
import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import de.adorsys.multibanking.xs2a_adapter.ApiException;
import de.adorsys.multibanking.xs2a_adapter.model.AccountReport;
import de.adorsys.multibanking.xs2a_adapter.model.HrefType;
import de.adorsys.multibanking.xs2a_adapter.model.TransactionList;
import de.adorsys.multibanking.xs2a_adapter.model.TransactionsResponse200Json;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Some banks (e.g. Fiducia) don't deliver all transactions in one chunk.
 * Instead they deliver for example the first 150 transactions in the first JSON and provide a "next" link.
 * This link must be used to fetch the next 150 transaction and so on until all transactions are fetched.
 * <p>
 * This class also parses the JSON and map it into TransactionsResponse
 * If a transaction details link is present in the Transaction, the link will be resolved
 * If no balance information is present in the transactions report, a separate call to the balance endpoint will be tried
 */
@Slf4j
public class PaginationResolver {
    private static final int MAX_PAGES = 50; // prevent infinite loops

    private final BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();
    private final PageResolver pageResolver;
    private final BalanceResolver balanceResolver;
    private final BalanceCalculator balanceCalculator;

    public PaginationResolver(String xs2aAdapterBaseUrl) {
        this.pageResolver = new PageResolver(xs2aAdapterBaseUrl);
        this.balanceResolver = new BalanceResolver(xs2aAdapterBaseUrl);
        this.balanceCalculator = new BalanceCalculator();
    }

    public TransactionsResponse toLoadBookingsResponse(TransactionsResponse200Json transactionsResponse200JsonTO, PaginationNextCallParameters nextCallParams) {
        List<Booking> bookings = extractBookings(transactionsResponse200JsonTO, AccountReport::getBooked);
        List<Booking> pendingBookings = extractBookings(transactionsResponse200JsonTO, AccountReport::getPending);

        BalancesReport balancesReport = balanceResolver.createBalancesReport(nextCallParams, transactionsResponse200JsonTO);

        // Pagination. If "next" link is present
        Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getTransactions)
            .map(AccountReport::getLinks)
            .map(linksAccountReport -> linksAccountReport.get("next"))
            .map(HrefType::getHref)
            .ifPresent(
                // resolve all pages here
                nextLink -> {
                    try {
                        BookingsAndBalance bookingsAndBalance = resolve(nextLink, nextCallParams);
                        bookings.addAll(bookingsAndBalance.getBookings());
                        pendingBookings.addAll(bookingsAndBalance.getPendingBookings());
                        if (bookingsAndBalance.getClosingBookedBalance() != null) {
                            balancesReport.setReadyBalance(bookingsAndBalance.getClosingBookedBalance());
                        }
                        if (bookingsAndBalance.getExpectedBalance() != null) {
                            balancesReport.setUnreadyBalance(bookingsAndBalance.getExpectedBalance());
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException("Error resolving transactions page", e);
                    }
                }
            );

        // reverse order - last booking must be first in the list
        if (!bookings.isEmpty()) {
            LocalDate firstBookingDate = bookings.get(0).getBookingDate();
            LocalDate lastBookingDate = bookings.get(bookings.size() - 1).getBookingDate();

            if (firstBookingDate != null && lastBookingDate != null && firstBookingDate.compareTo(lastBookingDate) < 0) {
                Collections.reverse(bookings); // just switch order of bookings without changing siblings
            }
        }

        // calculate balance after transaction
        balanceCalculator.calculateBalance(bookings, pendingBookings, balancesReport);

        return TransactionsResponse.builder()
            .bookings(bookings)
            .balancesReport(balancesReport)
            .build();
    }

    private BookingsAndBalance resolve(String nextLink, PaginationNextCallParameters nextCallParams) {
        List<Booking> bookings = new ArrayList<>();
        List<Booking> pendingBookings = new ArrayList<>();

        Balance closingBookedBalance = null;
        Balance expectedBalance = null;

        for (int i = 0; i < MAX_PAGES; i++) {
            BookingsAndBalance bookingsAndBalance = null;
            try {
                bookingsAndBalance = pageResolver.fetchNext(nextLink, nextCallParams);
            } catch (Exception e) {
                String message = e.getMessage();
                if (e instanceof ApiException) {
                    String responseBody = ((ApiException) e).getResponseBody();
                    if (StringUtils.isNotEmpty(responseBody)) {
                        message = responseBody;
                    }
                }
                log.error("Error fetching page " + i + ": " + message);
                log.error("We ignore this error and take what we got so far");
                break;
            }
            bookings.addAll(bookingsAndBalance.getBookings());
            pendingBookings.addAll(bookingsAndBalance.getPendingBookings());

            if (bookingsAndBalance.getClosingBookedBalance() != null) {
                closingBookedBalance = bookingsAndBalance.getClosingBookedBalance();
            }
            if (bookingsAndBalance.getExpectedBalance() != null) {
                expectedBalance = bookingsAndBalance.getExpectedBalance();
            }

            if (bookingsAndBalance.getNextLink() != null) {
                nextLink = bookingsAndBalance.getNextLink();
            } else {
                break;
            }
        }
        return BookingsAndBalance.builder()
            .bookings(bookings)
            .pendingBookings(pendingBookings)
            .closingBookedBalance(closingBookedBalance)
            .expectedBalance(expectedBalance)
            .build();
    }


    private List<Booking> extractBookings(TransactionsResponse200Json transactionsResponse200JsonTO, Function<AccountReport, TransactionList> mapper) {
        return Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getTransactions)
            .map(mapper)
            .stream()
            .flatMap(Collection::stream)
            .map(bankingGatewayMapper::toBooking)
            .collect(Collectors.toList());
    }

    @Data
    @Builder
    public static class BookingsAndBalance {
        private List<Booking> bookings;
        private List<Booking> pendingBookings;
        private Balance closingBookedBalance;
        private Balance expectedBalance;
        private String nextLink;
    }

    @Data
    @Builder
    public static class PaginationNextCallParameters {
        private BgSessionData bgSessionData;
        private String resourceId;
        private String consentId;
        private String bankCode;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private boolean withBalance;
    }

    @Data
    @Builder
    public static class AccountAndTransaction {
        private String account;
        private String transaction;
    }
}
