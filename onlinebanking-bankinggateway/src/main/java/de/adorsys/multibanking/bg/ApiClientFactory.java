package de.adorsys.multibanking.bg;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.adorsys.multibanking.banking_gateway_b2c.ApiClient;
import de.adorsys.multibanking.banking_gateway_b2c.api.AisApi;
import de.adorsys.multibanking.banking_gateway_b2c.api.OAuthApi;
import de.adorsys.multibanking.xs2a_adapter.JSON;
import de.adorsys.multibanking.xs2a_adapter.api.AccountInformationServiceAisApi;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okio.ByteString;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static de.adorsys.multibanking.xs2a_adapter.JSON.createGson;

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
            new HttpLoggingInterceptor(log::debug).setLevel(HttpLoggingInterceptor.Level.BODY)
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

        apiClient.getJSON().setGson(createGson()
            .registerTypeAdapter(Date.class, new JSON.DateTypeAdapter())
            .registerTypeAdapter(java.sql.Date.class, new JSON.SqlDateTypeAdapter())
            .registerTypeAdapter(OffsetDateTime.class, new JSON.OffsetDateTimeTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .registerTypeAdapter(byte[].class, new ByteArrayAdapter())
            .create());

        return apiClient;
    }

    public static OAuthApi bankingGatewayB2COAuthApi(String baseUrl) {
        return new OAuthApi(apiClientBankingGateway(baseUrl));
    }

    public static AisApi bankingGatewayB2CAisApi(String baseUrl, BgSessionData bgSessionData) {
        return bankingGatewayB2CAisApi(baseUrl, bgSessionData != null ? bgSessionData.getAccessToken() : null);
    }

    private static AisApi bankingGatewayB2CAisApi(String baseUrl, String accessToken) {
        AisApi b2CAisApi = new AisApi(apiClientBankingGateway(baseUrl));
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

        apiClient.getJSON().setGson(createGson()
            .registerTypeAdapter(Date.class, new JSON.DateTypeAdapter())
            .registerTypeAdapter(java.sql.Date.class, new JSON.SqlDateTypeAdapter())
            .registerTypeAdapter(OffsetDateTime.class, new JSON.OffsetDateTimeTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .registerTypeAdapter(byte[].class, new ByteArrayAdapter())
            .create());

        return apiClient;
    }

    private static class ByteArrayAdapter extends TypeAdapter<byte[]> {

        @Override
        public void write(JsonWriter out, byte[] value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(ByteString.of(value).base64());
            }
        }

        @Override
        public byte[] read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    String bytesAsBase64 = in.nextString();
                    ByteString byteString = ByteString.decodeBase64(bytesAsBase64);
                    return byteString.toByteArray();
            }
        }
    }

    /**
     * Gson TypeAdapter for JSR310 LocalDate type
     */
    private static class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {

        private DateTimeFormatter formatter;

        public LocalDateTypeAdapter() {
            this(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        public LocalDateTypeAdapter(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        public void setFormat(DateTimeFormatter dateFormat) {
            this.formatter = dateFormat;
        }

        @Override
        public void write(JsonWriter out, LocalDate date) throws IOException {
            if (date == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(date));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    String date = in.nextString();
                    return LocalDate.parse(date, formatter);
            }
        }
    }

}
