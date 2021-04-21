package de.adorsys.multibanking.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class WhitelistMaskEverythingLayout extends PatternLayout {
    private static final String USAGE_PATTERN = "(usage)\\=(.*?)[)|,](?= addkey=)"; // might contain commas
    private static final String JAVAOBJECT_PATTERN = "([\\w\\d]+)" + // key
        "\\=" +
        "([^()|,]*)" + // value
        "[)|,]";
    private static final String JSON_PATTERN = "\"([\\w\\d]+)\" ?" + // key
        "\\:" +
        " ?(\"[^\"]*\"|[^{}()\\[\\]|,\\s]*)" + // value
        "[\\s})|,]";

    private final Pattern multilinePattern;
    private List<String> whitelistEntries = new ArrayList<>();

    public WhitelistMaskEverythingLayout() {
        multilinePattern = Pattern.compile(
            String.join("|", USAGE_PATTERN, JAVAOBJECT_PATTERN, JSON_PATTERN),
            Pattern.MULTILINE
        );
    }

    public void addWhitelistEntry(String whitelistEntry) { // invoked for every single entry in the xml
        whitelistEntries.add(whitelistEntry);
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        return maskMessage(super.doLayout(event)); // calling superclass method is required
    }

    String maskMessage(String message) {
        if (multilinePattern == null) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message);
        Matcher matcher = multilinePattern.matcher(sb);
        while (matcher.find()) {
            // USAGE_PATTERN
            String key = matcher.group(1);
            String value = matcher.group(2);
            int valuePosition = 2;

            // JAVAOBJECT_PATTERN
            if (value == null) {
                key = matcher.group(3);
                value = matcher.group(4);
                valuePosition = 4;
            }

            // JSON_PATTERN
            if (value == null) {
                key = matcher.group(5);
                value = matcher.group(6);
                valuePosition = 6;
            }

            if (whitelistEntries.contains(key)) {
                continue;
            }

            if (value != null && !"null".equals(value)) {
                IntStream.range(matcher.start(valuePosition), matcher.end(valuePosition))
                    .forEach(i -> sb.setCharAt(i, '*')); // replace each character with asterisk
            }
        }
        String maskedMessage = sb.toString();
        return maskedMessage.replaceAll("\\*{10,}", "****###****");
    }
}
