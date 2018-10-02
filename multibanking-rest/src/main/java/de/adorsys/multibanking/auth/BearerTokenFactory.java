package de.adorsys.multibanking.auth;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class BearerTokenFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(BearerTokenFactory.class);

    private BearerTokenFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Extracts the stripped token from request header. This code is derived from TokenAuthenticationService
     */
    public static String extractToken(HttpServletRequest request) {
        final String TOKEN_PREFIX = "Bearer ";
        final String HEADER_KEY = "Authorization";

        String headerValue = request.getHeader(HEADER_KEY);
        if(StringUtils.isBlank(headerValue)) {
            if(LOGGER.isDebugEnabled()) LOGGER.debug("Header value '{}' is blank.", HEADER_KEY);
            return null;
        }

        if(!StringUtils.startsWithIgnoreCase(headerValue, TOKEN_PREFIX)) {
            if(LOGGER.isDebugEnabled()) LOGGER.debug("Header value does not start with '{}'.", TOKEN_PREFIX);
            return null;
        }

        return StringUtils.substringAfterLast(headerValue, " ");
    }
}
