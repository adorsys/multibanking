package de.adorsys.multibanking.encrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


/**
 * Created by alexg on 09.05.17.
 */
public class EncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(EncryptionUtil.class);

    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";

    public static String encrypt(String valueToEnc, SecretKey key) {
        try {
            Cipher encryptor = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            encryptor.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]));

            return Base64Utils.encodeToString(encryptor.doFinal(valueToEnc.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String encryptedValue, SecretKey key) {
        try {
            Cipher decryptor = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            decryptor.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
            byte[] decValue = decryptor.doFinal(Base64Utils.decodeFromString(encryptedValue));
            return new String(decValue, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}