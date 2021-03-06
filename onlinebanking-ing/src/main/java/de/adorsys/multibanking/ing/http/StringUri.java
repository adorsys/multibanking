package de.adorsys.multibanking.ing.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StringUri {
    private static final String SPACE = " ";
    private static final String ENCODED_SPACE = "%20";

    public static String fromElements(String... elements) {
        return Arrays.stream(elements)
                .map(StringUri::trimUri)
                .map(StringUri::formatUri)
                .collect(Collectors.joining("/"));
    }

    private static String trimUri(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        str = str.startsWith("/") ? str.substring(1) : str;
        return str.endsWith("/") ? str.substring(0, str.length() - 1) : str;
    }

    public static String withQuery(String uri, Map<String, ?> requestParams) {
        if (requestParams.isEmpty()) {
            return uri;
        }

        String requestParamsString = requestParams.entrySet().stream()
            .filter(entry -> entry.getKey() != null && entry.getValue() != null)
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));

        if (requestParamsString.isEmpty()) {
            return uri;
        }
        return uri + "?" + requestParamsString;
    }

    public static String withQuery(String uri, String paramName, String paramValue) {
        if (paramName == null || paramName.isEmpty()
                || paramValue == null || paramValue.isEmpty()) {
            return uri;
        }

        return uri + "?" + paramName + "=" + paramValue;
    }

    public static Map<String, String> getQueryParamsFromUri(String uri) {
        Map<String, String> queryParams = new HashMap<>();

        if (!uri.contains("?")) {
            return queryParams;
        }

        String paramsString = uri.split("\\?")[1];
        String[] paramsWithValues = paramsString.split("&");

        for (String paramWithValueString : paramsWithValues) {
            String[] paramAndValue = paramWithValueString.split("=");
            queryParams.put(paramAndValue[0], paramAndValue[1]);
        }

        return queryParams;
    }

    private static String formatUri(String uri) {
        return uri.replace(SPACE, ENCODED_SPACE);
    }
}
