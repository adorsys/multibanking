package de.adorsys.multibanking.correlation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
@RequiredArgsConstructor
public class CorrelationIdInterceptor extends HandlerInterceptorAdapter {
    @NonNull
    private final CorrelationId correlationId;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String cid = request.getHeader(CorrelationId.CORRELATION_ID);

        if (StringUtils.isNotEmpty(cid)) {
            correlationId.setCorrelationId(cid);
            log.info("Correlation ID on multibanking request: {}", correlationId.getCorrelationId());
        }

        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }
}
