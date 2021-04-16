package de.adorsys.multibanking.bg.mapper;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Three digits Geschaeftsvorfallcode can be mapped to the Buchungstext.
 * This class reads the gvcode to buchungstext csv and and also extracts the fallbacks
 * for the unknown gvcodes. For the unknown gvcodes the leading digit is considered.
 * The csv contains the fallback Buchungstext after the key 0XX, 1XX, 2XX etc.
 */
@Slf4j
public class Gvc2Buchungstext {
    private static final String MAPPINGFILE = "/gvc2buchungstext.csv";

    private static final Map<String, String> GVCODES = new HashMap();
    private static final Map<String, String> FALLBACKS = new HashMap();

    static {
        InputStream inputStream = Gvc2Buchungstext.class.getResourceAsStream(MAPPINGFILE);
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStream, "UTF-8"))) {

            reader.lines()
                .map(line -> line.split(";"))
                .collect(Collectors.toMap(l -> l[0], l -> l[1]))
                .forEach(
                    (gvcode, text) -> {
                        if (gvcode.matches("^\\d{3}$")) {
                            GVCODES.put(gvcode, text);
                        } else if (gvcode.matches("^\\dXX$")){
                            FALLBACKS.put(Character.toString(gvcode.charAt(0)), text);
                        } else {
                            log.error("Wrong GV Code in CSV: [{}]", gvcode);
                        }
                    }
                );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param gvcode
     * @return buchungstext or null if nothing matches
     */
    public static String gvcode2Buchungstext(String gvcode) {
        if(!gvcode.matches("^\\d{3}$")) {
            log.warn("Wrong GV Code format requested: [{}]", gvcode);
            return null;
        }

        String buchungstext = null;
        if (GVCODES.containsKey(gvcode)) {
            buchungstext = GVCODES.get(gvcode);
        } else if (FALLBACKS.containsKey(Character.toString(gvcode.charAt(0)))) {
            buchungstext = FALLBACKS.get(Character.toString(gvcode.charAt(0)));
        } else {
            log.warn("GV Code cannot be mapped to Buchungstext [{}]", gvcode);
        }
        return buchungstext;
    }
}
