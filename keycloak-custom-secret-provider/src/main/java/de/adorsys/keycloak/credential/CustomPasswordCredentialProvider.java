package de.adorsys.keycloak.credential;

import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.logging.Logger;
import org.keycloak.broker.oidc.util.JsonSimpleHttp;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.TokenManager;

import java.io.IOException;

/**
 * Created by alexg on 23.05.17.
 */
public class CustomPasswordCredentialProvider extends PasswordCredentialProvider {

    private static final Logger logger = Logger.getLogger(TokenManager.class);

    private static final String CREDENTIAL_TYPE = "custom_secret";

    public CustomPasswordCredentialProvider(KeycloakSession session) {
        super(session);
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        boolean returnValue = super.updateCredential(realm, user, input);
        if (returnValue) {

            try {

                JsonNode publicKeyResponse = JsonSimpleHttp.asJson(JsonSimpleHttp.doGet("url-des-server", session));
                String publicKey = publicKeyResponse.get("publicKey").asText();
                String userPass = ((UserCredentialModel) input).getValue();

                CredentialModel newCustomSecret = new CredentialModel();
                newCustomSecret.setType(CREDENTIAL_TYPE);
                newCustomSecret.setCreatedDate(Time.currentTimeMillis());
                newCustomSecret.setValue("");
                //TODO 16 stelliges mit dem public key des servers verschlüsseltes passwort benötigt

                disableCredentialType(realm, user, CREDENTIAL_TYPE);
                getCredentialStore().createCredential(realm, user, newCustomSecret);
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return returnValue;
    }
}
