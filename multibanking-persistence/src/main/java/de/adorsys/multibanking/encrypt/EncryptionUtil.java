package de.adorsys.multibanking.encrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

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
            Cipher encryptor = Cipher.getInstance(AES_CBC_PKCS5_PADDING);;
            encryptor.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]));

            return new BASE64Encoder().encode(encryptor.doFinal(valueToEnc.getBytes()));
        } catch (Exception e) {
            log.error("{} encrypting value.{}", e.getClass().getName(), valueToEnc);
            log.error(e.getMessage());
            return valueToEnc;
        }
    }

    public static String decrypt(String encryptedValue, SecretKey key) {
        try {
            Cipher decryptor = Cipher.getInstance(AES_CBC_PKCS5_PADDING);;
            decryptor.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
            byte[] decValue = decryptor.doFinal(new BASE64Decoder().decodeBuffer(encryptedValue));
            return new String(decValue);
        } catch (Exception e) {
            log.error("{} decrypting value.{}", e.getClass().getName(), encryptedValue);
            log.error(e.getMessage());
            return encryptedValue;
        }
    }

}