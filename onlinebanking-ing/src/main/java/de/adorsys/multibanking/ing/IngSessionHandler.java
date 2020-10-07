package de.adorsys.multibanking.ing;

import de.adorsys.multibanking.domain.Message;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.ing.api.TokenResponse;
import de.adorsys.multibanking.ing.oauth.IngOauth2Service;
import de.adorsys.multibanking.ing.oauth.Oauth2Service;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.multibanking.domain.exception.MultibankingError.MISSING_AUTHORISATION_CODE;
import static de.adorsys.multibanking.domain.exception.MultibankingError.TOKEN_EXPIRED;

@RequiredArgsConstructor
public class IngSessionHandler {

    private final IngOauth2Service oauth2Service;

    public void checkIngSession(IngSessionData ingSessionData, String authorisationCode) {
        TokenResponse tokenResponse = null;
        if (ingSessionData.getAccessToken() == null) {
            tokenResponse = Optional.ofNullable(authorisationCode)
                .map(this::getUserToken)
                .orElseThrow(() -> {
                    URI authorizationRequestUri = getAuthorisationUri(ingSessionData.getTppRedirectUri());
                    Message message = new Message();
                    message.setParamsMap(Collections.singletonMap("redirectUrl", authorizationRequestUri.toString()));
                    return new MultibankingException(MISSING_AUTHORISATION_CODE, 401, null, Collections.singletonList(message));
                });
        } else if (LocalDateTime.now().isAfter(ingSessionData.getExpirationTime())) {
            tokenResponse = Optional.ofNullable(ingSessionData.getRefreshToken())
                .map(this::refreshToken)
                .orElseThrow(() -> {
                    URI authorizationRequestUri = getAuthorisationUri(ingSessionData.getTppRedirectUri());
                    Message message = new Message();
                    message.setParamsMap(Collections.singletonMap("redirectUrl", authorizationRequestUri.toString()));
                    return new MultibankingException(TOKEN_EXPIRED, 401, null, Collections.singletonList(message));
                });
        }

        Optional.ofNullable(tokenResponse)
            .ifPresent(response -> {
                ingSessionData.setAccessToken(response.getAccessToken());
                ingSessionData.setRefreshToken(response.getRefreshToken());
                ingSessionData.setExpirationTime(LocalDateTime.now().plusSeconds(response.getExpiresInSeconds()));
            });
    }

    URI getAuthorisationUri(String tppRedirectUri) {
        Oauth2Service.Parameters params = new Oauth2Service.Parameters(Collections.singletonMap("redirect_uri"
            , tppRedirectUri));
        return oauth2Service.getAuthorizationRequestUri(params);
    }

    private TokenResponse refreshToken(String refreshToken) {
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put("grant_type", "refresh_token");
        parametersMap.put("refresh_token", refreshToken);

        return oauth2Service.getToken(new Oauth2Service.Parameters(parametersMap));
    }

    private TokenResponse getUserToken(String authorisationCode) {
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put("grant_type", "authorization_code");
        parametersMap.put("code", authorisationCode);

        return oauth2Service.getToken(new Oauth2Service.Parameters(parametersMap));
    }
}
