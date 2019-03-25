package de.adorsys.multibanking.mock;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

import java.io.IOException;

public class BearerTokenAuthorizationInterceptor implements ClientHttpRequestInterceptor {

    private final String bearerToken;

    public BearerTokenAuthorizationInterceptor(String bearerToken) {
        Assert.hasLength(bearerToken, "bearerToken must not be empty");
        this.bearerToken = bearerToken;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        request.getHeaders().add("Authorization", "Bearer " + bearerToken);
        return execution.execute(request, body);
    }

}
