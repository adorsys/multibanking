package org.adorsys.psd2.pop;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PrivateKeyReader {

	public static List<Key> exportKeys(KeyStore keyStore, char[] keypass){
		List<Key> result = new ArrayList<>();
		try {
			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				Key privateKey = keyStore.getKey(alias, keypass);
				if(privateKey!=null) result.add(privateKey);
			}
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
		
		return result;
	}
}
