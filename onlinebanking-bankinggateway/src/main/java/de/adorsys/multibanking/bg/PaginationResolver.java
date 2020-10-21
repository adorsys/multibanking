package de.adorsys.multibanking.bg;

import com.squareup.okhttp.Call;
import de.adorsys.multibanking.domain.Balance;
import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import de.adorsys.multibanking.xs2a_adapter.ApiResponse;
import de.adorsys.multibanking.xs2a_adapter.api.AccountInformationServiceAisApi;
import de.adorsys.multibanking.xs2a_adapter.model.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static de.adorsys.multibanking.bg.ApiClientFactory.accountInformationServiceAisApi;

/**
 * Some banks (e.g. Fiducia) don't deliver all transactions in one chunk.
 * Instead they deliver for example the first 150 transactions in the first JSON and provide a "next" link.
 * This link must be used to fetch the next 150 transaction and so on until all transactions are fetched.
 * This class also parses the JSON and map it into TransactionsResponse
 */
@Slf4j
public class PaginationResolver {
    private final static String FIDUCIA_PAGINATION_QUERY_PARAMETER = "scrollRef";
    private final static String COMMERZBANK_PAGINATION_QUERY_PARAMETER = "page";
    private final static int MAX_PAGES = 50; // prevent infinite loops

    private final String xs2aAdapterBaseUrl;
    private BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();

    public PaginationResolver(String xs2aAdapterBaseUrl) {
        this.xs2aAdapterBaseUrl = xs2aAdapterBaseUrl;
    }

    public TransactionsResponse jsonStringToLoadBookingsResponse(String json, PaginationNextCallParameters nextCallParams) throws Exception {
        TransactionsResponse200Json transactionsResponse200JsonTO =
            GsonConfig.getGson().fromJson(json, TransactionsResponse200Json.class);

        List<Booking> bookings = Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getTransactions)
            .map(AccountReport::getBooked)
            .map(transactions -> bankingGatewayMapper.toBookings(transactions))
            .orElse(Collections.emptyList());

        BalancesReport balancesReport = new BalancesReport();
        Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getBalances)
            .map(List::stream).orElse(Stream.empty())
            .filter(balance -> balance.getBalanceType() != null)
            .forEach(balance -> {
                switch (balance.getBalanceType()) {
                    case EXPECTED:
                        balancesReport.setUnreadyBalance(bankingGatewayMapper.toBalance(balance));
                        break;
                    case CLOSINGBOOKED:
                        balancesReport.setReadyBalance(bankingGatewayMapper.toBalance(balance));
                        break;
                    default:
                        // ignore
                        break;
                }
            });

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
                        if (bookingsAndBalance.getClosingBookedBalance() != null) {
                            balancesReport.setReadyBalance(bookingsAndBalance.getClosingBookedBalance());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error resolving transactions page", e);
                    }
                }
            );

        Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getBalances)
            .map(List::stream).orElse(Stream.empty())
            .filter(balance -> BalanceType.OPENINGBOOKED.equals(balance.getBalanceType()))
            .findFirst().ifPresent(
            openingbooked -> {
                BigDecimal balance = new BigDecimal(openingbooked.getBalanceAmount().getAmount());
                for(Booking booking : bookings) {
                    balance = balance.add(booking.getAmount());
                    booking.setBalance(balance);
                    booking.setExternalId(booking.getValutaDate() + "_" + booking.getAmount() + "_" + booking.getBalance()); // override fallback external id
                }
                Optional.ofNullable(balancesReport.getReadyBalance()).ifPresent(
                    readyBalance -> {
                        BigDecimal lastBookingBalance = bookings.get(bookings.size() - 1).getBalance();
                        if (!readyBalance.getAmount().equals(lastBookingBalance)) {
                            log.error("The closing booked balance {} and the calculated balance after the last transaction {} are not equal", readyBalance.getAmount(), lastBookingBalance);
                        }
                    }
                );
            }
        );

        return TransactionsResponse.builder()
            .bookings(bookings)
            .balancesReport(balancesReport)
            .build();
    }

    private BookingsAndBalance resolve(String nextLink, PaginationNextCallParameters nextCallParams) throws Exception {
        List<Booking> bookings = new ArrayList<>();
        Balance closingBookedBalance = null;
        for (int i = 0; i < MAX_PAGES; i++) {
            BookingsAndBalance bookingsAndBalance = fetchNext(nextLink, nextCallParams);
            bookings.addAll(bookingsAndBalance.getBookings());
            if (bookingsAndBalance.getClosingBookedBalance() != null) {
                closingBookedBalance = bookingsAndBalance.getClosingBookedBalance();
            }
            if (bookingsAndBalance.getNextLink() != null) {
                nextLink = bookingsAndBalance.getNextLink();
            } else {
                break;
            }
        }
        return BookingsAndBalance.builder()
            .bookings(bookings)
            .closingBookedBalance(closingBookedBalance)
            .build();
    }

    private BookingsAndBalance fetchNext(String nextLink, PaginationNextCallParameters params) throws Exception {
        String scrollRef = resolveScrollRef(nextLink);
        String page = resolvePage(nextLink);
        AccountInformationServiceAisApi aisApi = accountInformationServiceAisApi(xs2aAdapterBaseUrl,
            params.getBgSessionData());

        // Fiducia: "Only one of 'dateFrom' and 'scrollRef' may exist
        LocalDate dateFrom = null;
        LocalDate dateTo = null;

        // Commerzbank: Call must be equal
        if (scrollRef == null && page != null) {
            dateFrom = params.getDateFrom();
            dateTo = params.getDateTo();
        }

        Call aisCall = aisApi.getTransactionListCall(
            params.getResourceId(), "booked", UUID.randomUUID(),
            params.getConsentId(), null, params.getBankCode(), null, dateFrom,
            dateTo, null,
            null, params.isWithBalance(), null, null, null, null, null, null, null, null, null, null,
            null, null, null, scrollRef, page, null, null);

        ApiResponse<Object> apiResponse = aisApi.getApiClient().execute(aisCall, String.class);
        TransactionsResponse200Json transactionsResponse200JsonTO =
            GsonConfig.getGson().fromJson((String) apiResponse.getData(), TransactionsResponse200Json.class);

        List<Booking> bookings = Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getTransactions)
            .map(AccountReport::getBooked)
            .map(transactions -> bankingGatewayMapper.toBookings(transactions))
            .orElse(Collections.emptyList());

        Balance closingBookedBalance = Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getBalances)
            .map(List::stream).orElse(Stream.empty())
            .filter(balance -> BalanceType.CLOSINGBOOKED.equals(balance.getBalanceType()))
            .map(bankingGatewayMapper::toBalance)
            .findFirst()
            .orElse(null);

        // Pagination. If another "next" link is present
        String next = Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getTransactions)
            .map(AccountReport::getLinks)
            .map(linksAccountReport -> linksAccountReport.get("next"))
            .map(HrefType::getHref)
            .orElse(null);

        return BookingsAndBalance.builder()
            .bookings(bookings)
            .closingBookedBalance(closingBookedBalance)
            .nextLink(next)
            .build();
    }

    // CAUTION scrollRef is not part of berlin group spec
    // it is used by Fiducia, but can be different at other banks
    private String resolveScrollRef(String nextLink) throws Exception {
        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(nextLink).build().getQueryParams();
        String scrollRefUrlEncoded = parameters.toSingleValueMap().get(FIDUCIA_PAGINATION_QUERY_PARAMETER);
        return scrollRefUrlEncoded != null ? URLDecoder.decode(scrollRefUrlEncoded, "UTF-8") : null; // scroll ref contains special characters
    }

    private String resolvePage(String nextLink) throws Exception {
        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(nextLink).build().getQueryParams();
        String pageUrlEncoded = parameters.toSingleValueMap().get(COMMERZBANK_PAGINATION_QUERY_PARAMETER);
        return pageUrlEncoded != null ? URLDecoder.decode(pageUrlEncoded, "UTF-8") : null; // scroll ref contains special characters
    }

    @Data
    @Builder
    private static class BookingsAndBalance {
        private List<Booking> bookings;
        private Balance closingBookedBalance;
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
}
