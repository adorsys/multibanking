package de.adorsys.multibanking.correlation;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeignCorrelationIdInterceptor implements RequestInterceptor {

    @NonNull
    private final CorrelationId correlationId;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (StringUtils.isNotEmpty(correlationId.getCorrelationId())) {
            requestTemplate.header(CorrelationId.CORRELATION_ID, correlationId.getCorrelationId());
        }
    }
}
