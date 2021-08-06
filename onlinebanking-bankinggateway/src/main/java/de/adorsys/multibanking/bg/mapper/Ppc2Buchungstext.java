package de.adorsys.multibanking.bg.mapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps PurposeCode to Buchungstext
 */
public class Ppc2Buchungstext {
    private static final String MAPPINGFILE = "/ppc2buchungstext.csv";

    private static final Map<String, String> PPC;

    static {
        InputStream inputStream = Ppc2Buchungstext.class.getResourceAsStream(MAPPINGFILE);
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStream, "UTF-8"))) {

            PPC = reader.lines()
                .map(line -> line.split(";"))
                .collect(Collectors.toMap(l -> l[0], l -> l[1]));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param purposeCode
     * @return buchungstext or null if nothing matches
     */
    public static String ppc2Buchungstext(String purposeCode) {
        return Optional.ofNullable(purposeCode)
            .map(String::toUpperCase)
            .map(PPC::get)
            .orElse(null);
    }
}
