package de.adorsys.multibanking.config;

import ch.qos.logback.classic.ClassicConstants;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LoggingHandlerInterceptor extends HandlerInterceptorAdapter {

    private static final String BANK_ACCESS_ID = "accessId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        insertIntoMDC(request, handler);

        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
        clearMDC();
    }

    private void insertIntoMDC(HttpServletRequest httpServletRequest, Object handler) {
        if (handler instanceof HandlerMethod) {
            String accessId = new UriTemplate("api/v1/bankaccesses/{accessId}").match(httpServletRequest.getRequestURI()).get(
                "paymentId");
            Optional.ofNullable(accessId)
                .ifPresent(s -> MDC.put(BANK_ACCESS_ID, s));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId;
            if (authentication == null || authentication.getName().equalsIgnoreCase("anonymousUser")) {
                userId = "ANON-" + UUID.randomUUID();
            } else {
                userId = authentication.getName();
            }

            MDC.put(ClassicConstants.USER_MDC_KEY, userId);
            MDC.put(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY, httpServletRequest.getRemoteHost());
            MDC.put(ClassicConstants.REQUEST_REQUEST_URI, httpServletRequest.getRequestURI());
            MDC.put(ClassicConstants.REQUEST_METHOD, httpServletRequest.getMethod());
            MDC.put(ClassicConstants.REQUEST_QUERY_STRING, httpServletRequest.getQueryString());
            MDC.put(ClassicConstants.REQUEST_USER_AGENT_MDC_KEY, httpServletRequest.getHeader("User-Agent"));
            MDC.put(ClassicConstants.REQUEST_X_FORWARDED_FOR, httpServletRequest.getHeader("X-Forwarded-For"));

            StringBuffer requestURL = httpServletRequest.getRequestURL();
            if (requestURL != null) {
                MDC.put(ClassicConstants.REQUEST_REQUEST_URL, requestURL.toString());
            }
        }
    }

    private void clearMDC() {
        MDC.remove(ClassicConstants.USER_MDC_KEY);
        MDC.remove(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY);
        MDC.remove(ClassicConstants.REQUEST_REQUEST_URI);
        MDC.remove(ClassicConstants.REQUEST_QUERY_STRING);
        // removing possibly inexistent item is OK
        MDC.remove(ClassicConstants.REQUEST_REQUEST_URL);
        MDC.remove(ClassicConstants.REQUEST_METHOD);
        MDC.remove(ClassicConstants.REQUEST_USER_AGENT_MDC_KEY);
        MDC.remove(ClassicConstants.REQUEST_X_FORWARDED_FOR);
    }

    @UtilityClass
    public static class Logging {

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

        static String cleanAndReduce(byte[] entity, Charset charset) {
            StringBuilder sb = new StringBuilder();
            if (entity.length < MAX_ENTITY_SIZE) {
                sb.append(new String(entity, 0, entity.length, charset));
            } else {
                sb.append(new String(entity, 0, MAX_ENTITY_SIZE, charset)).append("...");
            }
            return clean(sb);
        }

        static Charset determineCharset(MediaType contentType) {
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
}
