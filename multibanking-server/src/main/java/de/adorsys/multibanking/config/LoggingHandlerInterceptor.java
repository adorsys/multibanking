package de.adorsys.multibanking.config;

import ch.qos.logback.classic.ClassicConstants;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
public class LoggingHandlerInterceptor extends HandlerInterceptorAdapter {

    public static final String BANK_ACCESS_ID = "accessId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        insertIntoMDC(request, handler);

        return super.preHandle(request, response, handler);
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
        clearMDC();
    }

    void insertIntoMDC(HttpServletRequest httpServletRequest, Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            RequestMapping mapping = handlerMethod.getBean().getClass().getAnnotation(RequestMapping.class);

            if (mapping.path().length > 0) {
                UriTemplate uriTemplate = new UriTemplate(mapping.path()[0]);

                Map<String, String> map = uriTemplate.match(httpServletRequest.getRequestURI());

                MDC.put(BANK_ACCESS_ID, map.get(BANK_ACCESS_ID));
            }

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

    void clearMDC() {
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


}
