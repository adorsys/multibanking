package de.adorsys.multibanking.service.interceptor;

import de.adorsys.multibanking.auth.UserContext;
import de.adorsys.onlinebanking.mock.BearerTokenAuthorizationInterceptor;
import de.adorsys.onlinebanking.mock.MockBanking;
import de.adorsys.sts.tokenauth.BearerToken;
import org.springframework.web.client.RestTemplate;

public class TokenBasedMockBanking extends MockBanking {

    private UserContext userContext;

    public TokenBasedMockBanking(UserContext userContext) {
        super();
        this.userContext = userContext;
    }

    @Override
    public RestTemplate getRestTemplate(String bankLogin, String bankCode, String pin) {
        RestTemplate restTemplate = new RestTemplate();
        BearerToken bearerToken = userContext.getBearerToken();
        if (bearerToken != null)
            restTemplate.getInterceptors().add(new BearerTokenAuthorizationInterceptor(bearerToken.getToken()));
        return restTemplate;
    }

}
