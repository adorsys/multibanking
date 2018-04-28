package de.adorsys.multibanking.service.crypto;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.adorsys.cryptoutils.exceptions.BaseException;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;

public class KeyGen {
	public static OctetSequenceKey newAESKey(String keyId, EncryptionMethod encryptionMethod){
		int keySize = 0;
		if(encryptionMethod.getName().contains("128")){
			keySize = 128;
		} else if (encryptionMethod.getName().contains("192")){
			keySize = 192;
		} else if (encryptionMethod.getName().contains("256")){
			keySize = 256;
		} else {
			throw new BaseException("Unsupported Keysize: " + encryptionMethod.getName() + ". Use algorithm with key size 128 or 192 or 256");
		}

		KeyGenerator keyGenerator;
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			throw new BaseException(e);
		}
		keyGenerator.init(keySize);
		SecretKey secretKey = keyGenerator.generateKey();
		return new OctetSequenceKey.Builder(secretKey)
			.keyID(keyId)
			.algorithm(JWEAlgorithm.DIR)
			.keyUse(KeyUse.ENCRYPTION).build();
	}
}
