/*
 * finAPI RESTful Services
 * finAPI RESTful Services
 *
 * OpenAPI spec version: v1.46.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package io.swagger.client.api;

import io.swagger.client.ApiException;
import io.swagger.client.model.AccessToken;
import io.swagger.client.model.BadCredentialsError;
import io.swagger.client.model.ErrorMessage;
import org.junit.Test;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for AuthorizationApi
 */
@Ignore
public class AuthorizationApiTest {

    private final AuthorizationApi api = new AuthorizationApi();

    
    /**
     * Get tokens
     *
     * finAPI implements the OAuth 2.0 Standard for authorizing applications and users within applications. OAuth uses the terminology of clients and users. A client represents an application that calls finAPI services. A service call might be in the context of a user of the client (e.g.: getting a user&#39;s bank connections), or outside any user context (e.g.: editing your client&#39;s configuration, or creating a new user for your client). In any case, every service call must be authorized by an access_token. This service can be used to get such an access_token, for either one of the client&#39;s users, or for the client itself. Also, this service can be used to refresh the access_token of a user that has previously requested an access_token.&lt;br/&gt;&lt;br/&gt;To get a token, you must always pass a valid client identifier and client secret (&#x3D;client credentials). You can get free client credentials for the sandbox &lt;a href&#x3D;&#39;http://www.finapi.io/jetzt-testen/&#39;&gt;here&lt;/a&gt;. Alternatively, you can also contact us at &lt;a href&#x3D;&#39;mailto:support@finapi.io&#39;&gt;support@finapi.io&lt;/a&gt;.&lt;br/&gt;&lt;br/&gt;The authorization process is similar for both a user within a client, and for the client itself: &lt;br/&gt;&amp;bull; To authorize a client (i.e. application), use &lt;code&gt;grant_type&#x3D;client_credentials&lt;/code&gt;&lt;br/&gt;&amp;bull; To authorize a user, use &lt;code&gt;grant_type&#x3D;password&lt;/code&gt;&lt;br/&gt;&lt;br/&gt;If the given parameters are valid, the service will respond with the authorization data. &lt;br/&gt;Here is an example of a response when authorizing a user: &lt;br/&gt;&lt;pre&gt;{    \&quot;access_token\&quot;: \&quot;yvMbx_TgwdYE0hgOVb8N4ZOvxOukqfjzYOGRZcJiCjQuRGkVIBfjjV3YG4zKTGiY2aPn2cQTGaQOT8uo5uo7_QOXts1s5UBSVuRHc6a8X30RrGBTyqV9h26SUHcZPNbZ\&quot;,    \&quot;token_type\&quot;: \&quot;bearer\&quot;,    \&quot;refresh_token\&quot;: \&quot;0b9KjiBVlZLz7a4HshSAIcFuscStiXT1VzT5mgNYwCQ_dWctTDsaIjedAhD1LpsOFJ7x6K8Emf8M3VOQkwNFR9FHijALYSQw2UeRwAC2MvrOKwfF1dHmOq5VEVYEaGf6\&quot;,    \&quot;expires_in\&quot;: 3600,    \&quot;scope\&quot;: \&quot;all\&quot; }&lt;/pre&gt;&lt;br/&gt;&lt;p&gt;Use the returned access_token for other service calls by sending it in a &#39;Authorization&#39; header, with the word &#39;Bearer&#39; in front of the token. Like this:&lt;/p&gt;&lt;pre&gt;Authorization: Bearer yvMbx_TgwdYE0hgOVb8N4ZOvxOukqfjzYOGRZcJiCjQuRGkVIBfjjV3YG4zKTGiY2aPn2cQTGaQOT8uo5uo7_QOXts1s5UBSVuRHc6a8X30RrGBTyqV9h26SUHcZPNbZ&lt;/pre&gt;&lt;p&gt;&lt;b&gt;WARNING&lt;/b&gt;: Sending the access_token as a request parameter is deprecated and will probably be no longer supported in the next release of finAPI. Please always send the access_token in the request header, as shown above.&lt;/p&gt;&lt;p&gt;By default, the access tokens have an expiration time of one hour (however, you can change this via the service PATCH /clientConfiguration). If a token has expired, then using the token for a service call will result in a HTTP code 401. To restore access you can simply get a new token (as it is described above) or use &lt;code&gt;grant_type&#x3D;refresh_token&lt;/code&gt; (which works for user-related tokens only). In the latter case you just have to pass the previously received &lt;code&gt;refresh_token&lt;/code&gt; for the user.&lt;/p&gt;&lt;p&gt;If the user that you want to authorize is not yet verified by the client (please see the &#39;isUserAutoVerificationEnabled&#39; flag in the Client Configuration), then the service will respond with HTTP code 403. If the user is locked (see &#39;maxUserLoginAttempts&#39; in the Client Configuration), the service will respond with HTTP code 423.&lt;/p&gt;&lt;p&gt;If the current role has no privileges to call a certain service (e.g. if a user tries to create a new user, or if a client tries to access user data outside of any user context), then the request will fail with the HTTP code 403.&lt;/p&gt;&lt;p&gt;&lt;b&gt;IMPORTANT NOTES:&lt;/b&gt;&lt;br/&gt;&amp;bull; Even though finAPI is not logging query parameters, it is still recommended to pass the parameters in the POST body instead of in the URL. Also, please set the Content-Type of your request to &#39;application/x-www-form-urlencoded&#39; when calling this service.&lt;br/&gt;&amp;bull; You should use this service only when you actually need a new token. As long as a token exists and has not expired, the service will always return the same token for the same credentials. Calling this service repeatedly with the same credentials contradicts the idea behind the tokens in OAuth, and will have a negative impact on the performance of your application. So instead of retrieving the same tokens over and over with this service, you should cache the tokens and re-use them as long as they have not expired - or at least as long as you&#39;re using the same tokens repeatedly, e.g. for the time of an active user session in your application.&lt;/p&gt;
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void getTokenTest() throws ApiException {
        String grantType = null;
        String clientId = null;
        String clientSecret = null;
        String refreshToken = null;
        String username = null;
        String password = null;
        AccessToken response = api.getToken(grantType, clientId, clientSecret, refreshToken, username, password);

        // TODO: test validations
    }
    
    /**
     * Revoke a token
     *
     * An additional endpoint for the OAuth 2.0 Standard, which allows clients to notify finAPI that a previously obtained refresh_token or access_token is no longer required. A successful request will invalidate the given token. The revocation of a particular token may also cause the revocation of related tokens and the underlying authorization grant. For token_type_hint&#x3D;access_token finAPI will invalidate only the given access_token. For token_type_hint&#x3D;refresh_token, finAPI will invalidate the refresh token and all access tokens based on the same authorization grant. If the token_type_hint is not defined, finAPI will revoke all access and refresh tokens (if applicable) that are based on the same authorization grant.&lt;br/&gt;&lt;br/&gt;Note that the service responds with HTTP status code 200 both if the token has been revoked successfully, and if the client submitted an invalid token.&lt;br/&gt;&lt;br/&gt;Note also that the client&#39;s access_token is required to authenticate the revocation.&lt;br/&gt;&lt;br/&gt;Here is an example of how to revoke a user&#39;s refresh_token (and therefore also his access tokens):&lt;pre&gt;Authorization: Bearer {client_access_token} POST /oauth/revoke?token&#x3D;{refresh_token}&amp;token_type_hint&#x3D;refresh_token&lt;/pre&gt;
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void revokeTokenTest() throws ApiException {
        String token = null;
        String tokenTypeHint = null;
        api.revokeToken(token, tokenTypeHint);

        // TODO: test validations
    }
    
}