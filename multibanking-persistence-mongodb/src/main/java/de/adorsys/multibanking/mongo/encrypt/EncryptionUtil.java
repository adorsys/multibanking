package de.adorsys.multibanking.mongo.encrypt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;

/**
 * Created by alexg on 09.05.17.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class EncryptionUtil {

    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";

    static String encrypt(String valueToEnc, SecretKey key) {
        try {
            Cipher encryptor = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            encryptor.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]));

            return Base64Utils.encodeToString(encryptor.doFinal(valueToEnc.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static String decrypt(String encryptedValue, SecretKey key) {
        try {
            Cipher decryptor = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            decryptor.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
            byte[] decValue = decryptor.doFinal(Base64Utils.decodeFromString(encryptedValue));
            return new String(decValue, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
