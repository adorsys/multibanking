package de.adorsys.multibanking.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class PatternMaskingLayout extends PatternLayout {

    private Pattern multilinePattern;
    private List<String> maskPatterns = new ArrayList<>();

    public void addMaskPattern(String maskPattern) { // invoked for every single entry in the xml
        maskPatterns.add(maskPattern);
        multilinePattern = Pattern.compile(
            String.join("|", maskPatterns), // build pattern using logical OR
            Pattern.MULTILINE
        );
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
            IntStream.rangeClosed(1, matcher.groupCount()).forEach(group -> {
                if (matcher.group(group) != null) {
                    IntStream.range(matcher.start(group), matcher.end(group))
                        .forEach(i -> sb.setCharAt(i, '*')); // replace each character with asterisk
                }
            });
        }
        String maskedMessage = sb.toString();
        return maskedMessage.replaceAll("\\*{10,}", "****###****");
    }
}
