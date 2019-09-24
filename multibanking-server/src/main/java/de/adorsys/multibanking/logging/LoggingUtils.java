package de.adorsys.multibanking.logging;

import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class LoggingUtils {

    private static final int MAX_ENTITY_SIZE = 1024;

    private static final String PATTERN_TEMPLATE = "((\"%s\"):(?<value>\".[^\"]+\"))";
    private static final String[] JSON_LOG_EXCLUDES = {
        "pin",
        "pin2",
        "accountNumber",
        "iban"
    };
    private static final List<Pattern> SUPRESS_VALUES = new ArrayList<>();

    static {
        for (String logExclude : JSON_LOG_EXCLUDES) {
            SUPRESS_VALUES.add(Pattern.compile(String.format(PATTERN_TEMPLATE, logExclude)));
        }
    }

    public static String cleanAndReduce(byte[] entity, Charset charset) {
        StringBuilder sb = new StringBuilder();
        if (entity.length < MAX_ENTITY_SIZE) {
            sb.append(new String(entity, 0, entity.length, charset));
        } else {
            sb.append(new String(entity, 0, MAX_ENTITY_SIZE, charset)).append("...");
        }
        return clean(sb);
    }

    public static Charset determineCharset(MediaType contentType) {
        if (contentType != null) {
            try {
                Charset charSet = contentType.getCharset();
                if (charSet != null) {
                    return charSet;
                }
            } catch (UnsupportedCharsetException e) {
                // ignore
            }
        }
        return StandardCharsets.UTF_8;
    }

    private static String clean(StringBuilder sb) {
        SUPRESS_VALUES.forEach(pattern -> {
            Matcher matcher = pattern.matcher(sb.toString());
            while (matcher.find()) {
                int start = matcher.start("value");
                int end = matcher.end("value");
                for (int i = start + 1; i < end - 1; i++) {
                    sb.setCharAt(i, 'x');
                }
            }
        });
        return sb.toString();
    }
}
