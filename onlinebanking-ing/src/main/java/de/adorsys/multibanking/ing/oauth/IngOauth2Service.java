package de.adorsys.multibanking.ing.oauth;

import de.adorsys.multibanking.ing.api.AuthorizationURLResponse;
import de.adorsys.multibanking.ing.api.TokenResponse;
import de.adorsys.multibanking.ing.http.StringUri;

import java.net.URI;
import java.time.LocalDateTime;

public class IngOauth2Service {

    private final Oauth2Api oauth2Api;
    private final ClientAuthenticationFactory clientAuthenticationFactory;

    private TokenResponse applicationToken;
    private LocalDateTime applicationTokenExpirationTime;

    public IngOauth2Service(Oauth2Api oauth2Api, ClientAuthenticationFactory clientAuthenticationFactory) {
        this.oauth2Api = oauth2Api;
        this.clientAuthenticationFactory = clientAuthenticationFactory;
    }

    public URI getAuthorizationRequestUri(Oauth2Service.Parameters parameters) {
        ClientAuthentication clientAuthentication =
            clientAuthenticationFactory.newClientAuthentication(getApplicationToken());
        AuthorizationURLResponse authorizationUrlResponse = oauth2Api.getAuthorizationUrl(clientAuthentication,
            parameters.getRedirectUri())
            .getBody();

        return URI.create(StringUri.withQuery(authorizationUrlResponse.getLocation(), parameters.asMap()));
    }

    private TokenResponse getApplicationToken() {
        if (applicationToken != null && LocalDateTime.now().isBefore(applicationTokenExpirationTime)) {
            return applicationToken;
        }

        ClientAuthentication clientAuthentication =
            clientAuthenticationFactory.newClientAuthenticationForApplicationToken();
        applicationToken = oauth2Api.getApplicationToken(clientAuthentication).getBody();
        applicationTokenExpirationTime = LocalDateTime.now().plusSeconds(applicationToken.getExpiresInSeconds());
        return applicationToken;
    }

    private String getClientId() {
        return getApplicationToken().getClientId();
    }

    public TokenResponse getToken(Oauth2Service.Parameters parameters) {
        ClientAuthentication clientAuthentication =
            clientAuthenticationFactory.newClientAuthentication(getApplicationToken());
        return oauth2Api.getCustomerToken(parameters, clientAuthentication)
            .getBody();
    }

    public ClientAuthentication getClientAuthentication(String accessToken) {
        return clientAuthenticationFactory.newClientAuthentication(getClientId(), accessToken);
    }
}
