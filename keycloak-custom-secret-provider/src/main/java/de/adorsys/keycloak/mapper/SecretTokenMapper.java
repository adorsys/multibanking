package de.adorsys.keycloak.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.JWEHeader.Builder;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.adorsys.envutils.EnvProperties;
import org.adorsys.jjwk.selector.JWEEncryptedSelector;
import org.adorsys.jjwk.selector.KeyExtractionException;
import org.adorsys.jjwk.selector.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.selector.UnsupportedKeyLengthException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.keycloak.common.util.UriUtils;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alexg on 23.05.17.
 */
public class SecretTokenMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    public static final String PROVIDER_ID = "secret-token-mapper";
    private byte[] secretEncryptionPassword;
    private static final String CREDENTIAL_TYPE = "custom_secret";


    @Override
    public void postInit(KeycloakSessionFactory factory) {
        String prop = EnvProperties.getEnvOrSysProp("SECRET_ENCRYPTION_PASSWORD", false);
        try {
            secretEncryptionPassword = prop.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        super.postInit(factory);
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "User Attribute";
    }

    @Override
    public String getHelpText() {
        return "Map a db user sercret attribute to token.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }


    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                            UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        String customSecretAttr;
        List<String> attribute = userSession.getUser().getAttribute(CREDENTIAL_TYPE);
        if (attribute == null || attribute.isEmpty()) {
            customSecretAttr = generateUserSecret(userSession);
        } else {
            customSecretAttr = attribute.iterator().next();
        }
        String serializedSecret = wrapSecretForResourceServer(customSecretAttr, userSession, session);
        if (serializedSecret != null) {
            token.getOtherClaims().put("custom_secret", serializedSecret);
        }

        return token;
    }

    /**
     * TODO implement caching.
     *
     * @param customSecretAttr
     * @param userSession
     * @param session
     * @return
     */
    private String wrapSecretForResourceServer(String customSecretAttr, UserSessionModel userSession, KeycloakSession session) {
        try {
            //decrypt with database encryption pass
            JWEObject jweObject = JWEObject.parse(customSecretAttr);
            JWEDecrypter decrypter = new DirectDecrypter(secretEncryptionPassword);
            jweObject.decrypt(decrypter);
            Payload payload = jweObject.getPayload();

            //encrypt with remote server public key
            String url = EnvProperties.getEnvOrSysProp(userSession.getRealm().getName().toUpperCase() + "_PUBLIC_KEY_URL", true);
            if (url == null) {
                return null;
            }
            JWKSet jwkSet = JWKSet.parse(getPublicKey(url));
            JWK jwk = jwkSet.getKeys().iterator().next();
            JWEEncrypter jweEncrypter = JWEEncryptedSelector.geEncrypter(jwk, null, null);
            // JWE encrypt secret.
            JWEObject jweObj = new JWEObject(getHeader(jwk), payload);
            jweObj.encrypt(jweEncrypter);
            return jweObj.serialize();
        } catch (ParseException | JOSEException | UnsupportedEncAlgorithmException  | KeyExtractionException | UnsupportedKeyLengthException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getPublicKey(String url) {
        HttpClient client = new DefaultHttpClient();
        try {
            HttpGet get = new HttpGet(url);
            try {
                HttpResponse response = client.execute(get);
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("invalid public key response: "+url);
                }
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();

                return IOUtils.toString(is, "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    private JWEHeader getHeader(JWK jwk) throws JOSEException {
        JWEHeader header;
        if (jwk instanceof RSAKey) {
            header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);
        } else if (jwk instanceof ECKey) {
            header = new JWEHeader(JWEAlgorithm.ECDH_ES_A128KW, EncryptionMethod.A192GCM);
        } else {
            return null;
        }
        return new JWEHeader.Builder(header).keyID(jwk.getKeyID()).build();
    }

    /**
     * TODO implement caching
     */
    private String generateUserSecret(UserSessionModel userSession) {
        String randomGraph = RandomStringUtils.randomGraph(16);
        Builder headerBuilder = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
        JWEObject jweObj = new JWEObject(headerBuilder.build(), new Payload(randomGraph));
        try {
            DirectEncrypter encrypter = new DirectEncrypter(secretEncryptionPassword);
            // You can now use the encrypter on one or more JWE objects
            // that you wish to secure
            jweObj.encrypt(encrypter);
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
        String customSecretAttr = jweObj.serialize();

        userSession.getUser().setAttribute(CREDENTIAL_TYPE, Arrays.asList(customSecretAttr));
        return customSecretAttr;
    }

}
