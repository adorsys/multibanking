package de.adorsys.keycloak.credential;

import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.credential.PasswordCredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

/**
 * Created by alexg on 23.05.17.
 */
public class CustomPasswordCredentialProviderFactory extends PasswordCredentialProviderFactory {

    public static final String PROVIDER_ID="db-secret-provider";

    @Override
    public PasswordCredentialProvider create(KeycloakSession session) {
        return new CustomPasswordCredentialProvider(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
