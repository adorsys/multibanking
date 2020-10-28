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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.multibanking.bg.ApiClientFactory.accountInformationServiceAisApi;

/**
 * Some banks (e.g. Fiducia) don't deliver all transactions in one chunk.
 * Instead they deliver for example the first 150 transactions in the first JSON and provide a "next" link.
 * This link must be used to fetch the next 150 transaction and so on until all transactions are fetched.
 *
 * This class also parses the JSON and map it into TransactionsResponse
 * If a transaction details link is present in the Transaction, the link will be resolved
 * If no balance information is present in the transactions report, a separate call to the balance endpoint will be tried
 */
@Slf4j
public class PaginationResolver {
    private final static String FIDUCIA_PAGINATION_QUERY_PARAMETER = "scrollRef";
    private final static String COMMERZBANK_PAGINATION_QUERY_PARAMETER = "page";
    private final static String TRANSACTION_DETAILS_LINK_KEY = "transactionDetails";
    private final static String BALANCES_LINK_KEY ="balances";
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
            .map(TransactionList::stream).orElse(Stream.empty())
            .map(transactionDetails -> resolveTransactionDetailsLink(nextCallParams, transactionDetails))
            .map(bankingGatewayMapper::toBooking)
            .collect(Collectors.toList());

        BalancesReport balancesReport = new BalancesReport();
        BalanceList balanceList = Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getBalances)
            .orElse(null);

        if (balanceList == null) {
            balanceList = resolveBalanceListFromLink(nextCallParams, transactionsResponse200JsonTO);
        }

        Optional.ofNullable(balanceList)
            .map(BalanceList::stream).orElse(Stream.empty())
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

        Optional.ofNullable(balanceList)
            .map(BalanceList::stream).orElse(Stream.empty())
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
            .map(TransactionList::stream).orElse(Stream.empty())
            .map(transactionDetails -> resolveTransactionDetailsLink(params, transactionDetails))
            .map(bankingGatewayMapper::toBooking)
            .collect(Collectors.toList());

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

    /**
     * Sometimes the transaction list does not contain all transaction details. Instead a transactionDetails link is provided.
     * If so, we try to fetch the detail by resolving this link
     *
     * @param params original call technical data
     * @param originalTransactionDetails
     * @return transactionDetails either resolved or the same as before in case of an error or if no link was inside
     */
    private TransactionDetails resolveTransactionDetailsLink(PaginationNextCallParameters params, TransactionDetails originalTransactionDetails) {
        if (originalTransactionDetails.getLinks() == null || !originalTransactionDetails.getLinks().containsKey(TRANSACTION_DETAILS_LINK_KEY)) { // no details
            return originalTransactionDetails;
        }
        String transactionDetailsLink = originalTransactionDetails.getLinks().get(TRANSACTION_DETAILS_LINK_KEY).getHref();
        AccountAndTransaction accountAndTransaction = resolveAccountAndTransaction(transactionDetailsLink);
        if (accountAndTransaction == null) {
            return originalTransactionDetails;
        }

        // fetch details
        AccountInformationServiceAisApi aisApi = accountInformationServiceAisApi(xs2aAdapterBaseUrl, params.getBgSessionData()); // should be cheap
        try {
            Call transactionDetailsCall = aisApi.getTransactionDetailsCall(accountAndTransaction.getAccount(), accountAndTransaction.getTransaction(),
                UUID.randomUUID(), params.getConsentId(), null, params.getBankCode(), null, null, null, null,
                null, null,null, null, null, null, null, null,
                null, null, null, null);
            ApiResponse<InlineResponse200> apiResponse = aisApi.getApiClient().execute(transactionDetailsCall, InlineResponse200.class);
            if (apiResponse == null || apiResponse.getStatusCode() > 299) {
                log.error("Wrong status code on transaction detail: " + apiResponse.getStatusCode());
            } else {
                return apiResponse.getData().getTransactionsDetails();
            }
        } catch (Exception e) {
            log.error("Exception fetching transaction detail: " + accountAndTransaction.getTransaction(), e);
        }

        return originalTransactionDetails;
    }

    AccountAndTransaction resolveAccountAndTransaction(String transactionDetailsLink) {
        List<String> pathSegments = UriComponentsBuilder.fromUriString(transactionDetailsLink).build().getPathSegments();
        int accountsIndex = pathSegments.lastIndexOf("accounts");
        if (accountsIndex == -1) {
            log.error("TransactionDetails link without accounts: " + transactionDetailsLink);
            return null;
        }
        int transactionsIndex = pathSegments.lastIndexOf("transactions");
        if (transactionsIndex == -1) {
            log.error("TransactionDetails link without transactions: " + transactionDetailsLink);
            return null;
        }
        if (pathSegments.size() < (transactionsIndex + 2)) {
            log.error("TransactionDetails link to short: " + transactionDetailsLink);
            return null;
        }
        return AccountAndTransaction.builder()
            .account(pathSegments.get(++accountsIndex))
            .transaction(pathSegments.get(++transactionsIndex))
            .build();
    }

    private BalanceList resolveBalanceListFromLink(PaginationNextCallParameters params, TransactionsResponse200Json transactionsResponse200JsonTO) {
        String balancesLink = Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getTransactions)
            .map(AccountReport::getLinks)
            .map(linksDownload -> linksDownload.get(BALANCES_LINK_KEY))
            .map(HrefType::getHref)
            .orElse(null);

        if (balancesLink == null) {
            return null;
        }

        List<String> pathSegments = UriComponentsBuilder.fromUriString(balancesLink).build().getPathSegments();
        int accountsIndex = pathSegments.lastIndexOf("accounts");
        if (accountsIndex == -1) {
            log.error("Balances link without accounts: " + balancesLink);
            return null;
        }
        String account = pathSegments.get(++accountsIndex);

        AccountInformationServiceAisApi aisApi = accountInformationServiceAisApi(xs2aAdapterBaseUrl, params.getBgSessionData());
        try {
            Call balanceCall = aisApi.getBalancesCall(account, UUID.randomUUID(), params.getConsentId(), null, params.getBankCode(), null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null);
            ApiResponse<ReadAccountBalanceResponse200> apiResponse = aisApi.getApiClient().execute(balanceCall, ReadAccountBalanceResponse200.class);
            if (apiResponse == null || apiResponse.getStatusCode() > 299) {
                log.error("Wrong status code on balance: " + apiResponse.getStatusCode());
            } else {
                return apiResponse.getData().getBalances();
            }
        } catch (Exception e) {
            log.error("Exception fetching balances for account: " + account, e);
        }
        return null;
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

    @Data
    @Builder
    static class AccountAndTransaction {
        private String account;
        private String transaction;
    }
}
