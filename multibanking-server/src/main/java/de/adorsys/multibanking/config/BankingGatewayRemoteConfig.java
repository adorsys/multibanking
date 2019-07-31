package de.adorsys.multibanking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import de.adorsys.multibanking.exception.BankingGatewayException;
import de.adorsys.multibanking.exception.domain.Message;
import de.adorsys.multibanking.exception.domain.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class BankingGatewayRemoteConfig {

    private final ObjectMapper objectMapper;
    @Value("${bankinggateway.consent.url:http://localhost:8086}")
    private String bankinggatewayConsentUrl;

    @Bean
    @Qualifier("bankinggateway")
    public RestTemplate restTemplate(AuthorizationClientRequestFactory authorizationClientRequestFactory) {
        final RestTemplate restTemplate =
            new RestTemplate(new BufferingClientHttpRequestFactory(authorizationClientRequestFactory));
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(bankinggatewayConsentUrl));
        restTemplate.setErrorHandler(new ErrorHandler());
        restTemplate.getInterceptors().add(new LoggingInterceptor("BANKINGGATEWAY"));
        return restTemplate;
    }

    private static class LoggingInterceptor implements ClientHttpRequestInterceptor {

        private String backend;

        LoggingInterceptor(String backend) {
            this.backend = backend;
        }

        @Override
        public @NonNull
        ClientHttpResponse intercept(HttpRequest request, @NonNull byte[] body,
                                     @NonNull ClientHttpRequestExecution execution) throws IOException {

            URI uri = request.getURI();

            String query = "";

            if (uri.getQuery() != null) {
                query = "?" + uri.getQuery() + " ";
            }

            Charset charset = Logging.determineCharset(request.getHeaders().getContentType());
            String requestString = Logging.cleanAndReduce(body, charset);

            log.trace("{} > {} {}{} {}", backend, request.getMethod(), request.getURI().getPath(), query,
                requestString);

            ClientHttpResponse response = execution.execute(request, body);

            String responseString = Logging.cleanAndReduce(ByteStreams.toByteArray(response.getBody()), charset);

            log.trace("{} < {} {}", backend, response.getStatusCode(), responseString);

            return response;
        }

    }

    private class ErrorHandler extends DefaultResponseErrorHandler {

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            String responseBody = new String(getResponseBody(response), getCharsetOrDefault(response));
            Message errorMessage = getErrorMessages(responseBody);
            if (errorMessage != null) {
                throw new BankingGatewayException(response.getStatusCode(), errorMessage);
            }

            super.handleError(response);
        }

        private Charset getCharsetOrDefault(ClientHttpResponse response) {
            Charset charset = getCharset(response);
            return charset != null ? charset : StandardCharsets.UTF_8;
        }

        private Message getErrorMessages(String responseBody) {
            try {
                Collection<Message> messages = new ObjectMapper().readValue(responseBody, Messages.class).getMessages();
                if (!messages.isEmpty()) {
                    return messages.iterator().next();
                }
            } catch (IOException e) {
                //ignore
            }
            return null;
        }
    }
}
