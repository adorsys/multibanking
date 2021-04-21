package de.adorsys.multibanking.bg.resolver;

import com.squareup.okhttp.Call;
import de.adorsys.multibanking.bg.PaginationResolver;
import de.adorsys.multibanking.bg.mapper.BankingGatewayMapper;
import de.adorsys.multibanking.bg.mapper.BankingGatewayMapperImpl;
import de.adorsys.multibanking.bg.utils.GsonConfig;
import de.adorsys.multibanking.domain.Balance;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.xs2a_adapter.ApiException;
import de.adorsys.multibanking.xs2a_adapter.ApiResponse;
import de.adorsys.multibanking.xs2a_adapter.api.AccountInformationServiceAisApi;
import de.adorsys.multibanking.xs2a_adapter.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.multibanking.bg.ApiClientFactory.accountInformationServiceAisApi;

/**
 * Resolves a single page in pagination
 */
@RequiredArgsConstructor
public class PageResolver {
    private static final String FIDUCIA_PAGINATION_QUERY_PARAMETER = "scrollRef";
    private static final String COMMERZBANK_PAGINATION_QUERY_PARAMETER = "page";

    private final String xs2aAdapterBaseUrl;
    private final BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();

    public PaginationResolver.BookingsAndBalance fetchNext(String nextLink, PaginationResolver.PaginationNextCallParameters params) throws ApiException {
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

        // bookings
        List<Booking> bookings = extractBookings(transactionsResponse200JsonTO, AccountReport::getBooked);
        List<Booking> pendingBookings = extractBookings(transactionsResponse200JsonTO, AccountReport::getPending);

        // balances
        Balance closingBookedBalance = extractBalance(transactionsResponse200JsonTO, BalanceType.CLOSINGBOOKED);
        Balance expectedBalance = extractBalance(transactionsResponse200JsonTO, BalanceType.EXPECTED);

        // Pagination. If another "next" link is present
        String next = Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getTransactions)
            .map(AccountReport::getLinks)
            .map(linksAccountReport -> linksAccountReport.get("next"))
            .map(HrefType::getHref)
            .orElse(null);

        return PaginationResolver.BookingsAndBalance.builder()
            .bookings(bookings)
            .pendingBookings(pendingBookings)
            .closingBookedBalance(closingBookedBalance)
            .expectedBalance(expectedBalance)
            .nextLink(next)
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

    private Balance extractBalance(TransactionsResponse200Json transactionsResponse200JsonTO, BalanceType balanceType) {
        return Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getBalances)
            .stream()
            .flatMap(Collection::stream)
            .filter(balance -> balanceType.equals(balance.getBalanceType()))
            .map(bankingGatewayMapper::toBalance)
            .findFirst()
            .orElse(null);
    }

    // CAUTION scrollRef is not part of berlin group spec
    // it is used by Fiducia, but can be different at other banks
    private String resolveScrollRef(String nextLink) {
        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(nextLink).build().getQueryParams();
        String scrollRefUrlEncoded = parameters.toSingleValueMap().get(FIDUCIA_PAGINATION_QUERY_PARAMETER);
        return scrollRefUrlEncoded != null ? URLDecoder.decode(scrollRefUrlEncoded, StandardCharsets.UTF_8) : null; // scroll ref contains special characters
    }

    private String resolvePage(String nextLink) {
        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(nextLink).build().getQueryParams();
        String pageUrlEncoded = parameters.toSingleValueMap().get(COMMERZBANK_PAGINATION_QUERY_PARAMETER);
        return pageUrlEncoded != null ? URLDecoder.decode(pageUrlEncoded, StandardCharsets.UTF_8) : null; // scroll ref contains special characters
    }
}
