package de.adorsys.multibanking.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.lang.reflect.Field;
import java.security.Security;

public class TestConstants {

    static void setup() {
//        turnOffEncPolicy();

        System.setProperty("mongo.databaseName", "multibanking");
        System.setProperty("KEYSTORE_PASSWORD", "test123");

        Security.addProvider(new BouncyCastleProvider());
    }

    public static void turnOffEncPolicy() {
        // Warning: do not do this for productive code. Download and install the jce unlimited strength policy file
        // see http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            field.setAccessible(true);
            field.set(null, Boolean.FALSE);
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace(System.err);
        }
    }

}
