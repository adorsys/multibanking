package de.adorsys.multibanking.ing;

import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import de.adorsys.multibanking.ing.http.ApacheHttpClient;
import de.adorsys.multibanking.ing.http.Pkcs12KeyStore;
import de.adorsys.multibanking.ing.oauth.ClientAuthenticationFactory;
import de.adorsys.multibanking.ing.oauth.IngOauth2Service;
import de.adorsys.multibanking.ing.oauth.Oauth2Api;
import de.adorsys.multibanking.ing.oauth.Oauth2Service;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

@Ignore
public class IngAdapterTest {

    private static final String BASE_URL = "https://api.sandbox.ing.com";
    private static final String KEYSTORE_FILENAME = "example_eidas.p12";
    private static final String QWAC_ALIAS = "example_eidas_client_tls";
    private static final String QSEAL_ALIAS = "example_eidas_client_signing";
    private static final String AUTHORISATION_CODE = "8b6cd77a-aa44-4527-ab08-a58d70cca286";

    private static IngAdapter ingAdapter;

    @BeforeClass
    public static void prepareAdapter() throws Exception {
        URL keyStoreUrl = IngAdapterTest.class.getClassLoader().getResource(KEYSTORE_FILENAME).toURI().toURL();
        ingAdapter = new IngAdapter(BASE_URL, keyStoreUrl.toString(), "", QWAC_ALIAS,
            QSEAL_ALIAS);
    }

    @Test
    public void testAis() throws Exception {
        URL keyStoreUrl = IngAdapterTest.class.getClassLoader().getResource(KEYSTORE_FILENAME).toURI().toURL();
        Pkcs12KeyStore keyStore = new Pkcs12KeyStore(keyStoreUrl, "".toCharArray());
        Oauth2Api oauth2Api = new Oauth2Api(BASE_URL, createHttpClient(keyStore));
        ClientAuthenticationFactory clientAuthenticationFactory = clientAuthenticationFactory(keyStore);
        IngOauth2Service oauth2Service = new IngOauth2Service(oauth2Api, clientAuthenticationFactory);

        URI authorizationRequestUri =
            oauth2Service.getAuthorizationRequestUri(new Oauth2Service.Parameters(new HashMap<>()));

        assert authorizationRequestUri.toString().startsWith("https://developer.ing.com/openbanking/get-started");

        TransactionRequest<LoadAccounts> loadAccountsRequest = new TransactionRequest<>(new LoadAccounts());
        loadAccountsRequest.setAuthorisationCode(AUTHORISATION_CODE);

        ingAdapter.loadBankAccounts(loadAccountsRequest);
    }

    private ClientAuthenticationFactory clientAuthenticationFactory(Pkcs12KeyStore keyStore) throws KeyStoreException,
        NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, CertificateEncodingException {
        X509Certificate qsealCertificate = keyStore.getQsealCertificate(QSEAL_ALIAS);
        PrivateKey qsealPrivateKey = keyStore.getQsealPrivateKey(QSEAL_ALIAS);
        return new ClientAuthenticationFactory(qsealCertificate, qsealPrivateKey);
    }

    private ApacheHttpClient createHttpClient(Pkcs12KeyStore keyStore) throws Exception {
        SSLContext sslContext = keyStore.getSslContext(QWAC_ALIAS);
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        SSLConnectionSocketFactory sslSocketFactory =
            new SSLConnectionSocketFactory(socketFactory, null, null, (HostnameVerifier) null);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().disableDefaultUserAgent();
        httpClientBuilder.setSSLSocketFactory(sslSocketFactory);
        return new ApacheHttpClient(httpClientBuilder.build());
    }

}
