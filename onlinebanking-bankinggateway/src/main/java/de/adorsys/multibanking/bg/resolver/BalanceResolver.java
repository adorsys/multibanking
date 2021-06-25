package de.adorsys.multibanking.bg.resolver;

import com.squareup.okhttp.Call;
import de.adorsys.multibanking.bg.PaginationResolver;
import de.adorsys.multibanking.bg.mapper.BankingGatewayMapper;
import de.adorsys.multibanking.bg.mapper.BankingGatewayMapperImpl;
import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.xs2a_adapter.ApiResponse;
import de.adorsys.multibanking.xs2a_adapter.api.AccountInformationServiceAisApi;
import de.adorsys.multibanking.xs2a_adapter.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.bg.ApiClientFactory.accountInformationServiceAisApi;

/**
 * Maps balances to balance report or resolves balances from balance link
 */
@Slf4j
@RequiredArgsConstructor
public class BalanceResolver {
    private static final String ACCOUNTS_LINK_KEY = "account";
    private final BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();
    private final String xs2aAdapterBaseUrl;

    public BalancesReport createBalancesReport(PaginationResolver.PaginationNextCallParameters nextCallParams, TransactionsResponse200Json transactionsResponse200JsonTO) {
        BalancesReport balancesReport = new BalancesReport();
        BalanceList balanceList = Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getBalances)
            .orElse(null);

        if (balanceList == null) {
            balanceList = resolveBalanceListFromLink(nextCallParams, transactionsResponse200JsonTO);
        }

        Optional.ofNullable(balanceList).ifPresentOrElse(
            bl -> log.info("Received the following balance types " + bl.stream().map(Balance::getBalanceType).filter(Objects::nonNull).map(BalanceType::toString).collect(Collectors.joining(" - "))),
            () -> log.info("No balances in balance resolver")
        );

        Optional.ofNullable(balanceList)
            .stream()
            .flatMap(Collection::stream)
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
        return balancesReport;
    }

    private BalanceList resolveBalanceListFromLink(PaginationResolver.PaginationNextCallParameters params, TransactionsResponse200Json transactionsResponse200JsonTO) {
        String accountLink = Optional.ofNullable(transactionsResponse200JsonTO)
            .map(TransactionsResponse200Json::getTransactions)
            .map(AccountReport::getLinks)
            .map(linksDownload -> linksDownload.get(ACCOUNTS_LINK_KEY))
            .map(HrefType::getHref)
            .orElse(null);

        if (accountLink == null) {
            log.error("No account link. Cannot fetch balances");
            return null;
        }

        List<String> pathSegments = UriComponentsBuilder.fromUriString(accountLink).build().getPathSegments();
        int accountsIndex = pathSegments.lastIndexOf("accounts");
        if (accountsIndex == -1) {
            log.error("Account link without accounts: " + accountLink);
            return null;
        }
        String account = pathSegments.get(++accountsIndex);

        AccountInformationServiceAisApi aisApi = accountInformationServiceAisApi(xs2aAdapterBaseUrl, params.getBgSessionData());
        try {
            Call balanceCall = aisApi.getBalancesCall(account, UUID.randomUUID(), params.getConsentId(), null, params.getBankCode(), null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
            ApiResponse<ReadAccountBalanceResponse200> apiResponse = aisApi.getApiClient().execute(balanceCall, ReadAccountBalanceResponse200.class);
            if (apiResponse == null || apiResponse.getStatusCode() > 299) {
                log.error("Wrong status code on balance: " + (apiResponse != null ? apiResponse.getStatusCode() : ""));
            } else {
                return apiResponse.getData().getBalances();
            }
        } catch (Exception e) {
            log.error("Exception fetching balances for account: " + account, e);
        }
        return null;
    }
}
