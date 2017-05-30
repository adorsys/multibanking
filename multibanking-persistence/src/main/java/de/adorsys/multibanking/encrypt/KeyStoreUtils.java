package de.adorsys.multibanking.encrypt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.PasswordLookup;
import de.adorsys.multibanking.domain.KeyStoreEntity;
import org.adorsys.encobject.domain.keystore.KeystoreData;
import org.adorsys.encobject.service.MissingKeyAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreProviderException;
import org.adorsys.encobject.service.WrongKeystoreCredentialException;
import org.adorsys.envutils.EnvProperties;
import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.security.auth.callback.CallbackHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Created by alexg on 30.05.17.
 */
public class KeyStoreUtils {

    public static JWKSet loadPrivateKeys(KeyStoreEntity keyStoreEntity) {
        String serverKeystoreName = EnvProperties.getEnvOrSysProp("SERVER_KEYSTORE_NAME", "multibanking-service-keystore");
        String serverKeystorePassword = EnvProperties.getEnvOrSysProp("keystore.password", true);
        if (StringUtils.isBlank(serverKeystorePassword))
            throw new IllegalStateException("Missing environment property keystore.password");

        char[] keyPairPassword = serverKeystorePassword.toCharArray();
        CallbackHandler storePassHandler = new PasswordCallbackHandler(keyPairPassword);

        KeyStore keyStore;
        try {
            byte[] keyStoreBytes = keyStoreEntity.getEncData();
            KeystoreData keystoreData =KeystoreData.parseFrom(keyStoreBytes);;
            keyStore = initKeystore(keystoreData, serverKeystoreName, storePassHandler);
        } catch (CertificateException | WrongKeystoreCredentialException
                | MissingKeystoreAlgorithmException | MissingKeystoreProviderException | MissingKeyAlgorithmException
                | IOException e) {
            throw new IllegalStateException(e);
        }

        return exportPrivateKeys(keyStore, keyPairPassword);
    }

    private static JWKSet exportPrivateKeys(KeyStore keyStore, char[] keypass) {
        PasswordLookup pwLookup = name -> keypass;
        try {
            return JWKSet.load(keyStore, pwLookup);
        } catch (KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    private static KeyStore initKeystore(KeystoreData keystoreData, String storeid, CallbackHandler handler)
            throws WrongKeystoreCredentialException, MissingKeystoreAlgorithmException,
            MissingKeystoreProviderException, MissingKeyAlgorithmException, CertificateException, IOException {
        try {
            return loadKeyStore(new ByteArrayInputStream(keystoreData.getKeystore().toByteArray()), storeid,
                    keystoreData.getType(), handler);
        } catch (UnrecoverableKeyException e) {
            throw new WrongKeystoreCredentialException(e);
        } catch (KeyStoreException e) {
            if (e.getCause() != null) {
                Throwable cause = e.getCause();
                if (cause instanceof NoSuchAlgorithmException) {
                    throw new MissingKeystoreAlgorithmException(cause.getMessage(), cause);
                }
                if (cause instanceof NoSuchProviderException) {
                    throw new MissingKeystoreProviderException(cause.getMessage(), cause);
                }
            }
            throw new IllegalStateException("Unidentified keystore exception", e);
        } catch (NoSuchAlgorithmException e) {
            throw new MissingKeyAlgorithmException(e.getMessage(), e);
        }
    }

    /**
     * Loads a key store. Given the store bytes, the store type
     *
     * @param in : the inputStream from which to read the keystore
     * @param storeId : The store id. This is passed to the callback handler to identify the requested password record.
     * @param storeType : the type of this key store. f null, the defaut java keystore type is used.
     * @param storePassSrc : the callback handler that retrieves the store password.
     * @throws KeyStoreException either NoSuchAlgorithmException or NoSuchProviderException
     * @throws NoSuchAlgorithmException  if the algorithm used to check the integrity of the keystore cannot be found
     * @throws CertificateException if any of the certificates in the keystore could not be loaded
     * @throws UnrecoverableKeyException if a password is required but not given, or if the given password was incorrect
     * @throws IOException if there is an I/O or format problem with the keystore data
     */
    private static KeyStore loadKeyStore(InputStream in, String storeId, String storeType, CallbackHandler storePassSrc) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, IOException {

        // Use default type if blank.
        if (StringUtils.isBlank(storeType))storeType = "UBER";

        KeyStore ks = KeyStore.getInstance(storeType, new BouncyCastleProvider());

        try {
            ks.load(in, PasswordCallbackUtils.getPassword(storePassSrc, storeId));
        } catch (IOException e) {
            // catch missing or wrong key.
            if(e.getCause()!=null && (e.getCause() instanceof UnrecoverableKeyException)){
                throw (UnrecoverableKeyException)e.getCause();
            } else if (e.getCause()!=null && (e.getCause() instanceof BadPaddingException)){
                throw new UnrecoverableKeyException(e.getMessage());
            }
            throw e;
        }
        return ks;
    }
}
