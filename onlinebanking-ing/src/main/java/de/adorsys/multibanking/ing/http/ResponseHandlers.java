package de.adorsys.multibanking.ing.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.multibanking.domain.exception.MultibankingException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.adorsys.multibanking.domain.exception.MultibankingError.INTERNAL_ERROR;

public class ResponseHandlers {

    private static final String APPLICATION_JSON = "application/json";
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=([^;]+)");

    private static final JsonMapper jsonMapper = new JsonMapper();

    private ResponseHandlers() {
    }

    public static <T> HttpClient.ResponseHandler<T> jsonResponseHandler(Class<T> klass) {
        return (statusCode, responseBody, responseHeaders) -> {
            if (statusCode == 204) {
                return null;
            }

            String contentType = responseHeaders.getHeader(RequestHeaders.CONTENT_TYPE);

            if (contentType != null && !contentType.startsWith(APPLICATION_JSON)) {
                throw new MultibankingException(INTERNAL_ERROR, String.format(
                    "Content type %s is not acceptable, has to start with %s", contentType, APPLICATION_JSON));
            }

            if (statusCode == 200 || statusCode == 201) {
                return jsonMapper.readValue(responseBody, klass);
            }

            throw responseException(statusCode, new PushbackInputStream(responseBody), responseHeaders);
        };
    }

    private static MultibankingException responseException(int statusCode,
                                                           PushbackInputStream responseBody,
                                                           ResponseHeaders responseHeaders) {
        if (isEmpty(responseBody)) {
            return new MultibankingException(INTERNAL_ERROR, statusCode, "empty response");
        }
        String originalResponse = toString(responseBody, responseHeaders);
        return new MultibankingException(INTERNAL_ERROR, statusCode, originalResponse);
    }

    private static boolean isEmpty(PushbackInputStream responseBody) {
        try {
            int nextByte = responseBody.read();
            if (nextByte == -1) {
                return true;
            }
            responseBody.unread(nextByte);
            return false;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static HttpClient.ResponseHandler<String> stringResponseHandler() {
        return (statusCode, responseBody, responseHeaders) -> {
            if (statusCode == 200) {
                return toString(responseBody, responseHeaders);
            }

            throw responseException(statusCode, new PushbackInputStream(responseBody), responseHeaders);
        };
    }

    private static String toString(InputStream responseBody, ResponseHeaders responseHeaders) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = responseBody.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }

            String charset = StandardCharsets.UTF_8.name();
            String contentType = responseHeaders.getHeader(RequestHeaders.CONTENT_TYPE);
            if (contentType != null) {
                Matcher matcher = CHARSET_PATTERN.matcher(contentType);
                if (matcher.find()) {
                    charset = matcher.group(1);
                }
            }

            return baos.toString(charset);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static class JsonMapper {
        private final ObjectMapper objectMapper;

        JsonMapper() {
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.registerModule(buildPsd2DateTimeDeserializerModule());
        }

        public String writeValueAsString(Object value) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        }

        public <T> T readValue(InputStream inputStream, Class<T> klass) {
            try {
                return objectMapper.readValue(inputStream, klass);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public <T> T readValue(String s, Class<T> klass) {
            try {
                return objectMapper.readValue(s, klass);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public <T> T convertValue(Object value, Class<T> klass) {
            return objectMapper.convertValue(value, klass);
        }

        private SimpleModule buildPsd2DateTimeDeserializerModule() {
            SimpleModule dateTimeModule = new SimpleModule();
            dateTimeModule.addDeserializer(OffsetDateTime.class, new Psd2DateTimeDeserializer());
            return dateTimeModule;
        }
    }
}
