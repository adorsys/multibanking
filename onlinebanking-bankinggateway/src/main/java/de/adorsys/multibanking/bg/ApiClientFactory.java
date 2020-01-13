package de.adorsys.multibanking.bg;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.adorsys.multibanking.banking_gateway_b2c.ApiClient;
import de.adorsys.multibanking.banking_gateway_b2c.api.BankingGatewayB2CAisApi;
import de.adorsys.multibanking.banking_gateway_b2c.api.BankingGatewayB2COAuthApi;
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
        return accountInformationServiceAisApi(baseUrl, bgSessionData.getAccessToken());
    }

    private static AccountInformationServiceAisApi accountInformationServiceAisApi(String baseUrl, String accessToken) {
        AccountInformationServiceAisApi accountInformationServiceAisApi =
            new AccountInformationServiceAisApi(apiClientXs2aAdapter(baseUrl));
        accountInformationServiceAisApi.getApiClient().getHttpClient().interceptors().add(new OkHttpHeaderInterceptor(accessToken));

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

    public static BankingGatewayB2COAuthApi bankingGatewayB2COAuthApi(String baseUrl) {
        return new BankingGatewayB2COAuthApi(apiClientBankingGateway(baseUrl));
    }

    public static BankingGatewayB2CAisApi bankingGatewayB2CAisApi(String baseUrl, BgSessionData bgSessionData) {
        return bankingGatewayB2CAisApi(baseUrl, bgSessionData != null ? bgSessionData.getAccessToken() : null);
    }

    private static BankingGatewayB2CAisApi bankingGatewayB2CAisApi(String baseUrl, String accessToken) {
        BankingGatewayB2CAisApi b2CAisApi = new BankingGatewayB2CAisApi(apiClientBankingGateway(baseUrl));
        b2CAisApi.getApiClient().getHttpClient().interceptors().add(new OkHttpHeaderInterceptor(accessToken));
        return b2CAisApi;
    }

    private static ApiClient apiClientBankingGateway(String baseUrl) {
        return apiClientBankingGateway(baseUrl, null, null);
    }

    private static ApiClient apiClientBankingGateway(String baseUrl, String acceptHeader, String contentTypeHeader) {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(600, TimeUnit.SECONDS);

        client.interceptors().add(
            new HttpLoggingInterceptor(log::debug).setLevel(HttpLoggingInterceptor.Level.BODY)
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
