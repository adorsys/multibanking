package de.adorsys.multibanking.bg.mapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps BankTransactionCode to Buchungstext
 */
public class Btc2Buchungstext {
    private static final String MAPPINGFILE = "/btc2buchungstext.csv";

    private static final Map<String, String> BTC;

    static {
        InputStream inputStream = Btc2Buchungstext.class.getResourceAsStream(MAPPINGFILE);
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStream, "UTF-8"))) {

            BTC = reader.lines()
                .map(line -> line.split(";"))
                .collect(Collectors.toMap(l -> l[0], l -> l[1]));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param bankTransactionCode
     * @return buchungstext or null if nothing matches
     */
    public static String btc2Buchungstext(String bankTransactionCode) {
        return Optional.ofNullable(bankTransactionCode)
            .map(String::toUpperCase)
            .map(BTC::get)
            .orElse(null);
    }
}
