package de.adorsys.multibanking.ing;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * Utility class for building up a keystore which can be used in
 * SSL communication.
 */
public class KeyStoreUtil {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    static PrivateKey loadPrivateKey(String keyPath) throws IOException, GeneralSecurityException {
        try (Reader reader = new FileReader(keyPath);
             PEMParser parser = new PEMParser(reader)) {
            Object readObject;
            while ((readObject = parser.readObject()) != null) {
                if (readObject instanceof PEMKeyPair) {
                    PEMKeyPair keyPair = (PEMKeyPair) readObject;
                    return generatePrivateKey(keyPair.getPrivateKeyInfo());
                } else if (readObject instanceof PrivateKeyInfo) {
                    return generatePrivateKey((PrivateKeyInfo) readObject);
                }
            }
        }
        throw new GeneralSecurityException("Cannot generate private key from file: " + keyPath);
    }

    private static PrivateKey generatePrivateKey(PrivateKeyInfo keyInfo) throws IOException {
        return new JcaPEMKeyConverter().getPrivateKey(keyInfo);
    }

    private static void addCA(KeyStore keyStore, String caPath) throws IOException, KeyStoreException,
        CertificateException {
        for (Certificate cert : loadCertificates(caPath)) {
            X509Certificate crt = (X509Certificate) cert;
            String alias = crt.getSubjectX500Principal().getName();
            keyStore.setCertificateEntry(alias, crt);
        }
    }

    private static Certificate[] loadCertificates(String certPath) throws IOException, CertificateException {
        try (InputStream is = new FileInputStream(certPath)) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
            Collection<? extends Certificate> certs = certificateFactory.generateCertificates(is);
            return certs.toArray(new Certificate[certs.size()]);
        }
    }
}
