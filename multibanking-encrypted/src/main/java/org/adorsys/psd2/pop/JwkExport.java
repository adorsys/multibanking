package org.adorsys.psd2.pop;

import java.security.KeyStore;
import java.security.KeyStoreException;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.PasswordLookup;

public class JwkExport {
	
	public static JWKSet exportPrivateKeys(KeyStore keyStore, char[] keypass){
		PasswordLookup pwLookup = new PasswordLookup() {
			@Override
			public char[] lookupPassword(String name) {
				return keypass;
			}
		};
		try {
			return JWKSet.load(keyStore, pwLookup);
		} catch (KeyStoreException e) {
			throw new IllegalStateException(e);
		}
	}

	public static JWKSet exportPublicKeys(KeyStore keyStore, char[] keypass){
		JWKSet exportKeys = exportPrivateKeys(keyStore, keypass);
		return exportKeys.toPublicJWKSet();
	}
}
