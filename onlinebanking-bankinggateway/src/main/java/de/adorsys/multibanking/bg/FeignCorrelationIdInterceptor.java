package de.adorsys.multibanking.bg;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;

import java.util.Optional;

import static de.adorsys.multibanking.bg.Constants.CORRELATION_ID;
import static de.adorsys.multibanking.bg.Constants.CORRELATION_ID_HEADER;

public class FeignCorrelationIdInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Optional.ofNullable(MDC.get(CORRELATION_ID)).ifPresent(correlationId -> {
            requestTemplate.header(CORRELATION_ID_HEADER, correlationId);
        });
    }
}
