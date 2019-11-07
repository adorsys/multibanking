package de.adorsys.multibanking.ing.oauth;

import de.adorsys.multibanking.ing.api.AuthorizationURLResponse;
import de.adorsys.multibanking.ing.api.TokenResponse;
import de.adorsys.multibanking.ing.http.HttpClient;
import de.adorsys.multibanking.ing.http.Request;
import de.adorsys.multibanking.ing.http.StringUri;
import de.adorsys.multibanking.ing.model.Response;

import static de.adorsys.multibanking.ing.http.ResponseHandlers.jsonResponseHandler;
import static java.util.Collections.singletonMap;

public class Oauth2Api {
    private static final String TOKEN_ENDPOINT = "/oauth2/token";
    private static final String AUTHORIZATION_ENDPOINT = "/oauth2/authorization-server-url";

    private final String baseUri;
    private final HttpClient httpClient;

    public Oauth2Api(String baseUri, HttpClient httpClient) {
        this.baseUri = baseUri;
        this.httpClient = httpClient;
    }

    Response<TokenResponse> getApplicationToken(Request.Builder.Interceptor clientAuthentication) {
        // When using eIDAS certificates supporting PSD2 the scope parameter is not required.
        // The scopes will be derived automatically from the PSD2 roles in the certificate.
        // When using eIDAS certificates supporting PSD2, the response will contain the client ID of your application,
        // this client ID has to be used in the rest of the session when the client ID or key ID is required.
        return httpClient.post(baseUri + TOKEN_ENDPOINT)
            .urlEncodedBody(singletonMap("grant_type", "client_credentials"))
            .send(clientAuthentication, jsonResponseHandler(TokenResponse.class));
    }

    Response<TokenResponse> getCustomerToken(Oauth2Service.Parameters parameters,
                                             Request.Builder.Interceptor clientAuthentication) {

        return httpClient.post(baseUri + TOKEN_ENDPOINT)
            .urlEncodedBody(parameters.asMap())
            .send(clientAuthentication, jsonResponseHandler(TokenResponse.class));
    }

    Response<AuthorizationURLResponse> getAuthorizationUrl(Request.Builder.Interceptor clientAuthentication,
                                                           String redirectUri) {
        return httpClient.get(StringUri.withQuery(baseUri + AUTHORIZATION_ENDPOINT, "redirect_uri", redirectUri))
            .send(clientAuthentication, jsonResponseHandler(AuthorizationURLResponse.class));
    }
}
