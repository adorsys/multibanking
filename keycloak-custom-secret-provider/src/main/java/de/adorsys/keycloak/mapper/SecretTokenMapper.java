package de.adorsys.keycloak.mapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.adorsys.envutils.EnvProperties;
import org.adorsys.jjwk.selector.JWEEncryptedSelector;
import org.adorsys.jjwk.selector.KeyExtractionException;
import org.adorsys.jjwk.selector.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.selector.UnsupportedKeyLengthException;
import org.apache.commons.lang3.RandomStringUtils;
import org.keycloak.broker.oidc.util.JsonSimpleHttp;
import org.keycloak.models.ClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEHeader.Builder;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

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
                                            UserSessionModel userSession, ClientSessionModel clientSession) {

    	String customSecretAttr;
    	List<String> attribute = userSession.getUser().getAttribute(CREDENTIAL_TYPE);
    	if(attribute==null || attribute.isEmpty()){    		
    		customSecretAttr = generateUserSecret(userSession);
        } else {
        	customSecretAttr = attribute.iterator().next();
        }
    	String serializedSecret = wrapSecretForResourceServer(customSecretAttr, userSession, session);
		token.getOtherClaims().put("custom_secret", serializedSecret);
        return token;
    }

    /**
     * TODO implement caching.
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
			String url = EnvProperties.getEnvOrSysProp(userSession.getRealm().getName().toUpperCase()+"_PUBLIC_KEY_URL", false);
			JsonNode publicKeyResponse = JsonSimpleHttp.asJson(JsonSimpleHttp.doGet(url, session));
			JWKSet jwkSet = JWKSet.parse(publicKeyResponse.toString());
			JWK jwk = jwkSet.getKeys().iterator().next();
			JWEEncrypter jweEncrypter = JWEEncryptedSelector.geEncrypter(jwk, null, null);
			// JWE encrypt secret.
			JWEObject jweObj = new JWEObject(getHeader(jwk), payload);
			jweObj.encrypt(jweEncrypter);
			return jweObj.serialize();
		} catch (ParseException | JOSEException | UnsupportedEncAlgorithmException | IOException | KeyExtractionException | UnsupportedKeyLengthException e) {
			throw new IllegalStateException(e);
		}

	}

	private JWEHeader getHeader(JWK jwk) throws JOSEException {
        if (jwk instanceof RSAKey) {
            return new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);
        } else if (jwk instanceof ECKey) {
            return new JWEHeader(JWEAlgorithm.ECDH_ES_A128KW, EncryptionMethod.A192GCM);
        }
        return null;
    }
    
	/**
	 * TODO implement caching
	 */
    private String generateUserSecret(UserSessionModel userSession){
    	String randomGraph = RandomStringUtils.randomGraph(16);
		Builder headerBuilder = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
		JWEObject jweObj = new JWEObject(headerBuilder.build(), new Payload(randomGraph));
		try {
			DirectEncrypter encrypter = new DirectEncrypter(secretEncryptionPassword);
			// You can now use the encrypter on one or more JWE objects 
			// that you wish to secure
			jweObj.encrypt(encrypter);
		} catch (JOSEException e){
			throw new IllegalStateException(e);
		}
		String customSecretAttr = jweObj.serialize();
    	
		userSession.getUser().setAttribute(CREDENTIAL_TYPE, Arrays.asList(customSecretAttr));
		return customSecretAttr;
    }

}
