package de.adorsys.multibanking.config;

import de.adorsys.sts.tokenauth.BearerToken;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationClientRequestFactory extends HttpComponentsClientHttpRequestFactory implements ClientHttpRequestFactory {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Autowired
    private BearerToken bearerToken;

    public AuthorizationClientRequestFactory() {
        super(HttpClients.custom()
            .disableCookieManagement()
            .build()
        );
    }

    @Override
    protected void postProcessHttpRequest(HttpUriRequest request) {
        request.setHeader(AUTHORIZATION_HEADER, bearerToken.getToken());
    }
}
