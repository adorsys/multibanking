package de.adorsys.keycloak.credential;

import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.logging.Logger;
import org.keycloak.broker.oidc.util.JsonSimpleHttp;
import org.keycloak.common.util.Base64;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.TokenManager;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by alexg on 23.05.17.
 */
public class CustomPasswordCredentialProvider extends PasswordCredentialProvider {

    private static final Logger logger = Logger.getLogger(TokenManager.class);

    private static final String CREDENTIAL_TYPE = "custom_secret";
    private Cipher asymmetricCipher;

    public CustomPasswordCredentialProvider(KeycloakSession session) {
        super(session);
        try {

        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        boolean returnValue = super.updateCredential(realm, user, input);
        if (returnValue) {
            try {
                String url = System.getenv(realm.getName().toUpperCase()+"_PUBLIC_KEY_URL");
                if (url != null) {
                    PublicKey publicKey = getPublicKey(url);

                    Cipher asymmetricCipher = Cipher.getInstance("RSA/NONE/NoPadding", "BC");
                    asymmetricCipher.init(Cipher.ENCRYPT_MODE, publicKey);

                    String userPass = ((UserCredentialModel) input).getValue();
                    byte[] encUserPass = asymmetricCipher.doFinal(userPass.getBytes());

                    CredentialModel newCustomSecret = new CredentialModel();
                    newCustomSecret.setType(CREDENTIAL_TYPE);
                    newCustomSecret.setCreatedDate(Time.currentTimeMillis());
                    newCustomSecret.setValue(new String(encUserPass));
                    //TODO 16 stelliges mit dem public key des servers verschlüsseltes passwort benötigt

                    disableCredentialType(realm, user, CREDENTIAL_TYPE);
                    getCredentialStore().createCredential(realm, user, newCustomSecret);
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return returnValue;
    }

    private PublicKey getPublicKey(String url) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        JsonNode publicKeyResponse = JsonSimpleHttp.asJson(JsonSimpleHttp.doGet(url, session));
        String publicKeyString = publicKeyResponse.get("keys").get(0).get("x5c").asText();
        byte[] byteKey = Base64.decode(publicKeyString.getBytes());
        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(X509publicKey);
    }
}
