package de.adorsys.multibanking.bg;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.adorsys.multibanking.banking_gateway_b2c.ApiClient;
import de.adorsys.multibanking.banking_gateway_b2c.api.BankingGatewayB2CAisApi;
import de.adorsys.multibanking.xs2a_adapter.api.AccountInformationServiceAisApi;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@UtilityClass
public class ApiClientFactory {

    public static AccountInformationServiceAisApi accountInformationServiceAisApi(String baseUrl,
                                                                                  BgSessionData bgSessionData) {
        return Optional.ofNullable(bgSessionData)
            .map(BgSessionData::getAccessToken)
            .map(token -> {
                AccountInformationServiceAisApi aisApi = accountInformationServiceAisApi(baseUrl);
                aisApi.getApiClient().setAccessToken(token);
                return aisApi;
            })
            .orElseGet(() -> accountInformationServiceAisApi(baseUrl));
    }

    private static AccountInformationServiceAisApi accountInformationServiceAisApi(String baseUrl) {
        AccountInformationServiceAisApi accountInformationServiceAisApi =
            new AccountInformationServiceAisApi(apiClientXs2aAdapter(baseUrl));
        accountInformationServiceAisApi.getApiClient().getHttpClient().interceptors().clear();
        accountInformationServiceAisApi.getApiClient().getHttpClient().interceptors().add(
            new HttpLoggingInterceptor(log::debug)
                .setLevel(HttpLoggingInterceptor.Level.BODY)
        );
        accountInformationServiceAisApi.getApiClient().getHttpClient().interceptors().add(new OkHttpCorrelationIdInterceptor());

        return accountInformationServiceAisApi;
    }

    private static de.adorsys.multibanking.xs2a_adapter.ApiClient apiClientXs2aAdapter(String baseUrl) {
        return apiClientXs2aAdapter(baseUrl, null, null);
    }

    private static de.adorsys.multibanking.xs2a_adapter.ApiClient apiClientXs2aAdapter(String baseUrl,
                                                                                       String acceptHeader,
                                                                                       String contentTypeHeader) {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(600, TimeUnit.SECONDS);
        client.interceptors().add(
            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        );

        de.adorsys.multibanking.xs2a_adapter.ApiClient apiClient =
            new de.adorsys.multibanking.xs2a_adapter.ApiClient() {
                @Override
                public String selectHeaderAccept(String[] accepts) {
                    return Optional.ofNullable(acceptHeader)
                        .orElseGet(() -> super.selectHeaderAccept(accepts));
                }

                @Override
                public String selectHeaderContentType(String[] contentTypes) {
                    return Optional.ofNullable(contentTypeHeader)
                        .orElseGet(() -> super.selectHeaderContentType(contentTypes));
                }

            };

        apiClient.setHttpClient(client);
        apiClient.setBasePath(baseUrl);
        return apiClient;
    }

    public static BankingGatewayB2CAisApi bankingGatewayB2CAisApi(String baseUrl, BgSessionData bgSessionData) {
        return Optional.ofNullable(bgSessionData)
            .map(BgSessionData::getAccessToken)
            .map(token -> {
                BankingGatewayB2CAisApi aisApi = bankingGatewayB2CAisApi(baseUrl);
                aisApi.getApiClient().setAccessToken(token);
                return aisApi;
            })
            .orElseGet(() -> bankingGatewayB2CAisApi(baseUrl));
    }

    private static BankingGatewayB2CAisApi bankingGatewayB2CAisApi(String baseUrl) {
        BankingGatewayB2CAisApi b2CAisApi = new BankingGatewayB2CAisApi(apiClientBankingGateway(baseUrl));
        b2CAisApi.getApiClient().getHttpClient().interceptors().clear();
        b2CAisApi.getApiClient().getHttpClient().interceptors().add(
            new HttpLoggingInterceptor(log::debug)
                .setLevel(HttpLoggingInterceptor.Level.BODY)
        );
        b2CAisApi.getApiClient().getHttpClient().interceptors().add(new OkHttpCorrelationIdInterceptor());

        return b2CAisApi;
    }

    private static ApiClient apiClientBankingGateway(String baseUrl) {
        return apiClientBankingGateway(baseUrl, null, null);
    }

    private static ApiClient apiClientBankingGateway(String baseUrl, String acceptHeader, String contentTypeHeader) {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(600, TimeUnit.SECONDS);
        client.interceptors().add(
            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        );

        ApiClient apiClient = new ApiClient() {
            @Override
            public String selectHeaderAccept(String[] accepts) {
                return Optional.ofNullable(acceptHeader)
                    .orElseGet(() -> super.selectHeaderAccept(accepts));
            }

            @Override
            public String selectHeaderContentType(String[] contentTypes) {
                return Optional.ofNullable(contentTypeHeader)
                    .orElseGet(() -> super.selectHeaderContentType(contentTypes));
            }

        };

        apiClient.setHttpClient(client);
        apiClient.setBasePath(baseUrl);
        return apiClient;
    }

}
