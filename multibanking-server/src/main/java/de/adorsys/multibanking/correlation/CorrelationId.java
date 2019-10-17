package de.adorsys.multibanking.correlation;

import lombok.Data;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class CorrelationId {
    public static final String CORRELATION_ID = "Correlation-ID";
    private String correlationId;
}
